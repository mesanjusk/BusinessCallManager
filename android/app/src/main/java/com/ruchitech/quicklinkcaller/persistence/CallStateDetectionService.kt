package com.ruchitech.quicklinkcaller.persistence

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.os.SystemClock
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.Logger
import com.ruchitech.quicklinkcaller.helper.NotificationHelper
import com.ruchitech.quicklinkcaller.helper.isServiceRunning
import com.ruchitech.quicklinkcaller.helper.syncAppLogs
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ACTION_HEARTBEAT
import com.ruchitech.quicklinkcaller.persistence.McsConstants.CALL_STATE_OFFHOOK
import com.ruchitech.quicklinkcaller.persistence.McsConstants.CALL_STATE_RINGING
import com.ruchitech.quicklinkcaller.persistence.McsConstants.FIVE_SEC_DELAY
import com.ruchitech.quicklinkcaller.persistence.McsConstants.HEARTBEAT_INITIATED
import com.ruchitech.quicklinkcaller.persistence.McsConstants.INITIATING_MANUAL_WORK
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ONE_MINUTE
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ONE_MINUTE_FIFTEEN_SECONDS
import com.ruchitech.quicklinkcaller.persistence.McsConstants.PERIODIC_5_S
import com.ruchitech.quicklinkcaller.persistence.McsConstants.SERVICE_STARTED
import com.ruchitech.quicklinkcaller.persistence.McsConstants.TWENTY_SECONDS
import com.ruchitech.quicklinkcaller.persistence.McsConstants.TWO_MINUTE
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ZERO
import com.ruchitech.quicklinkcaller.persistence.foreground_notification.ForegroundServiceContext
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_ADD_NOTE
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_NOTIFICATION_REPLY
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_REMINDER
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_SHARE_CONTACT
import com.ruchitech.quicklinkcaller.persistence.recievers.ServiceControlReceiver
import com.ruchitech.quicklinkcaller.persistence.recievers.TriggerReceiver
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.CallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.stopAppCallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class MyIntent {
    data class UpdateNotes(val number: String, val note: String) : MyIntent()
    data class UpdateTasks(val number: String, val tasks: List<Tasks?>) : MyIntent()

    data object ClearData : MyIntent()
}

private fun parseIntent(intent: Intent?): MyIntent? {
    return when {
        intent?.getBooleanExtra("clearData", false) == true -> {
            MyIntent.ClearData
        }

        intent?.getBooleanExtra("notes", false) == true -> {
            MyIntent.UpdateNotes(
                number = intent.getStringExtra("number") ?: "",
                note = intent.getStringExtra("note") ?: ""
            )
        }

        intent?.getBooleanExtra("tasks", false) == true -> {
            val activeCallTasksJson = intent.getStringExtra("tasksList")
            val mapType3 = object : TypeToken<List<Tasks?>>() {}.type
            val activeCallTasks: List<Tasks?> = Gson().fromJson(activeCallTasksJson, mapType3)
            MyIntent.UpdateTasks(
                number = intent.getStringExtra("number") ?: "", tasks = activeCallTasks
            )
        }

        else -> null
    }
}

class CallLogObserver(handler: Handler?) : ContentObserver(handler) {
    private var lastEventTime: Long = 0
    private val debounceInterval = 3000L // 1 second
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastEventTime
        if (timeDifference >= debounceInterval) {
            MyApp.instance.updateCallLogs()
            lastEventTime = currentTime
        }

    }
}


@AndroidEntryPoint
class CallStateDetectionService : Service(), Handler.Callback {

    private val CHANNEL_ID = "notification_for_calls"
    private val CHANNEL_NAME = "Call notification"
    private val NOTIFICATION_ID = 1

    private var connectIntent: Intent? = null
    private var powerManager: PowerManager? = null
    private var alarmManager: AlarmManager? = null
    private var heartbeatIntent: PendingIntent? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var logger: Logger
    private val debounceInterval = 1000L // 1 second
    private var lastEventTime: Long = 0
    private val handlerToStopForeground: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val appPreferences by lazy { AppPreferences(applicationContext) }
    private val telephonyManager: TelephonyManager by lazy {
        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    private var lastHeartbeatTime = 0L
    private var lastPushHeartbeatTime = 0L
    private val serviceJob by lazy { Job() }
    private val serviceScope by lazy { CoroutineScope(Dispatchers.IO + serviceJob) }
    private val phoneCallReceiver by lazy { PhoneCallReceiver() }
    private val callLogObserver: CallLogObserver by lazy {
        CallLogObserver(Handler(Looper.getMainLooper()))
    }

    // Create a mutable map to store notification IDs and timestamps
    val notificationQueue = mutableMapOf<Int, Long>()

    @Inject
    lateinit var dbRepository: DbRepository

    @Inject
    lateinit var callLogHelper: CallLogHelper

    @Inject
    lateinit var contactHelper: ContactHelper

    @Inject
    lateinit var appPreference: AppPreference
    val activeCalls: MutableMap<String, Int> = mutableMapOf()
    val activeCallsNote: MutableMap<String, String> = mutableMapOf()
    val activeTasks: MutableMap<String, List<Tasks?>> = mutableMapOf()
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (val myIntent = parseIntent(intent)) {
                MyIntent.ClearData -> {
                    activeCalls.clear()
                    activeCallsNote.clear()
                    activeTasks.clear()
                }

                is MyIntent.UpdateNotes -> {
                    activeCallsNote[myIntent.number] = myIntent.note
                }

                is MyIntent.UpdateTasks -> {
                    activeTasks[myIntent.number] = myIntent.tasks
                }

                null -> {}
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        appPreference.lastHearBeatTime = 0L
        val logFile = File(getExternalFilesDir(null), "app_log.txt")
        logger = Logger("InsetsController024", logFile)
        syncAppLogs()
        val callLogUri = CallLog.Calls.CONTENT_URI
        val callLogObserver = CallLogObserver(Handler(Looper.getMainLooper()))
        contentResolver.registerContentObserver(callLogUri, true, callLogObserver)
        TriggerReceiver.register(this)
        val intentFilter = IntentFilter("com.example.ACTION_CALL_STATE_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                registerReceiver(
                    broadcastReceiver,
                    intentFilter, Context.RECEIVER_NOT_EXPORTED,
                )
            } else {
                registerReceiver(
                    broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED
                )
            }
        } else {
            registerReceiver(
                broadcastReceiver, intentFilter
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                registerReceiver(
                    phoneCallReceiver,
                    IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED),
                    Context.RECEIVER_NOT_EXPORTED,
                )
            } else {
                registerReceiver(
                    phoneCallReceiver,
                    IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED),
                    Context.RECEIVER_NOT_EXPORTED
                )
            }
        } else {
            registerReceiver(
                phoneCallReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            )
        }

        startPlaying()
        heartbeatIntent = PendingIntent.getService(
            this,
            ZERO,
            Intent(ACTION_HEARTBEAT, null, this, CallStateDetectionService::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        synchronized(CallStateDetectionService::class.java) {
            if (handlerThread == null) {
                val handlerThread2 = HandlerThread()
                handlerThread = handlerThread2
                handlerThread2.start()
            }
        }
        //  telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun startSilentPlayback(duration: Long = 1000) {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    mediaPlayer?.seekTo(ZERO)
                    mediaPlayer?.start()
                    Thread.sleep(duration)
                    logger.logInfo("engageInertProcessing: $duration")
                    mediaPlayer?.pause()
                } else {
                    logger.logError("Failed to engageInertProcessing already resumed")
                }
            }
        } catch (e: IllegalStateException) {
            startPlaying()
            logger.logError("engageInertProcessing:${e.message}")
        }

    }

    private fun startPlaying() {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer()
                val uri = Uri.parse("android.resource://$packageName/${R.raw.silence_no_sound}")
                mediaPlayer?.setDataSource(this, uri)
                mediaPlayer?.setScreenOnWhilePlaying(false)
                mediaPlayer?.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer
                mediaPlayer?.prepare()
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
                Handler().postDelayed({
                    mediaPlayer?.pause()
                }, FIVE_SEC_DELAY)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun isIncomingCallsEnabled(): Boolean {
        var value = false
        value = dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions?.contains(
            AllCallerIdOptions.Incoming
        ) == true

        return value
    }

    // Function to check if outgoing calls option is selected
    private suspend fun isOutgoingCallsEnabled(): Boolean {
        var value = false
        value = dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions?.contains(
            AllCallerIdOptions.Outgoing
        ) == true

        return value
    }

    // Function to check if post calls option is selected
    private suspend fun isPostCallsEnabled(): Boolean {

        return dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions?.contains(
            AllCallerIdOptions.Post
        ) == true
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var handler: Handler? = Handler()
        var obtainMessage = Message()
        ForegroundServiceContext.completeForegroundService(
            this, intent, "CallStateDetectionService"
        )
        if (rootHandler == null) {
            if (connectIntent == null) {
                connectIntent = intent
            } else if (intent != null) {
            }
            return START_REDELIVER_INTENT
        } else if (intent == null) {
            return START_REDELIVER_INTENT
        } else {
            try {
                val obj =
                    if (intent.hasExtra(McsConstants.EXTRA_REASON)) intent.extras!![McsConstants.EXTRA_REASON] else intent
                if (ACTION_HEARTBEAT == intent.action) {
                    lastHeartbeatTime = System.currentTimeMillis()
                    logger.logInfo("Heartbeat triggered at ${getCurrentTime(lastHeartbeatTime)}")
                    serviceScope.launch {
                        ///     appPreference.lastHearBeatTime = System.currentTimeMillis()
                        dbRepository.timestampDao.updateLastHeartbeat(System.currentTimeMillis())
                    }
                    handler = rootHandler
                    obtainMessage = handler!!.obtainMessage(HEARTBEAT_INITIATED, obj)
                } else if (McsConstants.ACTION_CONNECT == intent.action) {
                    handler = rootHandler
                    obtainMessage = handler!!.obtainMessage(SERVICE_STARTED, obj)
                } else if (McsConstants.REMINDER == intent.action) {
                    triggerAlarm(intent.getLongExtra("alarmID", 0L))
                    stopForeground(true)
                }
                handler!!.sendMessage(obtainMessage)
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            } catch (e: RuntimeException) {
                logger.logInfo("on create RuntimeException exception ${e.message}")
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        //telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        mediaPlayer?.release()
        serviceJob.cancel()
        unregisterReceiver(phoneCallReceiver)
        unregisterReceiver(broadcastReceiver)
        contentResolver.unregisterContentObserver(callLogObserver)

    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
    }

    override fun stopService(name: Intent): Boolean {
        return super.stopService(name)
    }


    private fun showCustomNotification(context: Context, callType: String, callingNumber: String) {
        var contactName = ""
        var numberFrom = 0
        var colorForName: Int
        val notificationId = callingNumber.hashCode() // Use phone number hashcode as notification ID
        CoroutineScope(Dispatchers.IO).launch {
            var checkName: String? = ""
            val contactDetails = contactHelper.getNameFromPhoneNumber(callingNumber)
            if (contactDetails.isEmpty() || contactDetails == "Unknown") {
                checkName = dbRepository.contact.getContactByPhoneNumber(callingNumber)?.contact_title
                contactName = if (!checkName.isNullOrEmpty()) {
                    numberFrom = 2
                    colorForName = R.color.orange
                    checkName
                } else {
                    colorForName = R.color.orange
                    numberFrom = 0
                    "Unknown"
                }
            } else {
                numberFrom = 1
                colorForName = R.color.theme_purple
                val alsoCheckInAppDB =
                    dbRepository.contact.getContactByPhoneNumber(callingNumber)?.contact_title
                if (!alsoCheckInAppDB.isNullOrEmpty()) {
                    numberFrom = 3
                }
                contactName = contactDetails.ifEmpty { "Unknown" }
            }

            // Auto-create lead for unknown callers
            if (numberFrom == 0) {
                val userUuid = MyApp.instance.appPreference.userId
                if (userUuid != null) {
                    val existing = MyApp.instance.dbRepository.leadDao.getLeadByPhone(callingNumber)
                    if (existing == null) {
                        MyApp.instance.dbRepository.leadDao.insertLead(
                            Lead(
                                lead_uuid = java.util.UUID.randomUUID().toString(),
                                user_uuid = userUuid,
                                phone = callingNumber,
                                source = "call",
                                status = "New",
                                notes = "[]",
                                call_refs = "[]",
                                isSynced = false
                            )
                        )
                    }
                }
            }

            delay(150)
            val callIntent = Intent(context, PostCallActivity::class.java).apply {
                action = NotificationReceiver.ACTION_CALL
                putExtra("type", callType)
                putExtra("number", callingNumber)
                putExtra("name", contactName)
            }
            val callPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val textMsgIntent = Intent(context, PostCallActivity::class.java).apply {
                action = NotificationReceiver.ACTION_TEXT_MSG
                putExtra("type", callType)
                putExtra("number", callingNumber)
                putExtra("name", contactName)
            }
            val textMsgPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                textMsgIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val openWhatsappIntent = Intent(context, PostCallActivity::class.java).apply {
                action = NotificationReceiver.ACTION_WHATSAPP
                putExtra("type", callType)
                putExtra("number", callingNumber)
                putExtra("name", contactName)
            }
            val actionWhatsappPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openWhatsappIntent,
                FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val remoteInput = androidx.core.app.RemoteInput.Builder("key_text_reply")
                .setLabel("Enter your note here...").build()

            val replyPendingIntent = PendingIntent.getBroadcast(
                context, notificationId, Intent(context, NotificationReceiver::class.java).apply {
                    action = ACTION_NOTIFICATION_REPLY
                    putExtra("type", callType)
                    putExtra("number", callingNumber)
                    putExtra("notificationId", notificationId)
                }, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val addNotePendingIntent = PendingIntent.getActivity(
                context, notificationId, Intent(context, PostCallActivity::class.java).apply {
                    action = ACTION_ADD_NOTE
                    putExtra("type", callType)
                    putExtra("number", callingNumber)
                    putExtra("notificationId", notificationId)
                }, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val shareContactPendingIntent = PendingIntent.getActivity(
                context, notificationId, Intent(context, PostCallActivity::class.java).apply {
                    action = ACTION_SHARE_CONTACT
                    putExtra("type", callType)
                    putExtra("number", callingNumber)
                    putExtra("notificationId", notificationId)
                }, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )


            val reminderPendingIntent = PendingIntent.getActivity(
                context, notificationId, Intent(context, PostCallActivity::class.java).apply {
                    action = ACTION_REMINDER
                    putExtra("type", callType)
                    putExtra("number", callingNumber)
                    putExtra("notificationId", notificationId)
                }, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val savaeContactPendingIntent = PendingIntent.getActivity(
                context, notificationId, Intent(context, PostCallActivity::class.java).apply {
                    action = NotificationReceiver.ACTION_SAVE_CONTACT
                    putExtra("type", callType)
                    putExtra("number", callingNumber)
                    putExtra("notificationId", notificationId)
                }, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val replyAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_input_add, "Add note", replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            val replyAction2 = NotificationCompat.Action.Builder(
                android.R.drawable.ic_input_add, "cancel", addNotePendingIntent
            ).build()

            val addLeadIntent = Intent(context, PostCallActivity::class.java).apply {
                action = NotificationReceiver.ACTION_ADD_LEAD
                putExtra("type", callType)
                putExtra("number", callingNumber)
                putExtra("name", contactName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val addLeadPendingIntent = PendingIntent.getActivity(
                context, notificationId + 100, addLeadIntent,
                FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val copyNumberIntent = Intent(context, PostCallActivity::class.java).apply {
                action = NotificationReceiver.ACTION_COPY_NUMBER
                putExtra("type", callType)
                putExtra("number", callingNumber)
                putExtra("name", contactName)
            }
            val actionCopyNumPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                copyNumberIntent,
                FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val shareContactWith = PendingIntent.getActivity(
                context, notificationId, Intent(context, PostCallActivity::class.java).apply {
                    action = NotificationReceiver.ACTION_SHARE_NUMBER
                    putExtra("type", callType)
                    putExtra("contactName", contactName)
                    putExtra("number", callingNumber)
                    putExtra("notificationId", notificationId)
                }, FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            createNotificationChannel(context, callingNumber)
            // Create a custom notification layout
            val notificationLayout = RemoteViews(context.packageName, R.layout.notification_ui)

            notificationLayout.setOnClickPendingIntent(R.id.ivNote, addNotePendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.ivCopy, actionCopyNumPendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.ivShareContact, shareContactWith)
            notificationLayout.setOnClickPendingIntent(R.id.ivAddContact, savaeContactPendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.ivCall, callPendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.ivTextMsg, textMsgPendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.ivWhatsapp, actionWhatsappPendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.ivReminder, reminderPendingIntent)
            notificationLayout.setOnClickPendingIntent(
                R.id.ivShareContactWith, shareContactPendingIntent
            )
            notificationLayout.setTextViewText(R.id.tvName, contactName)
            notificationLayout.setTextColor(R.id.tvName, resources.getColor(colorForName))
            notificationLayout.setTextViewText(R.id.tvNumber, callingNumber)

            // Build the notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .apply {
                    if (numberFrom == 0) {
                        addAction(android.R.drawable.ic_menu_add, "Add Lead", addLeadPendingIntent)
                    }
                }
            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
            notificationQueue[notificationId] = Date().time
        }
    }

    private fun createNotificationChannel(context: Context, callerId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private suspend fun insertNoteOnCallLog(note: String?, callerId: String) {
        callLogHelper.insertRecentCallLogs {}
        delay(100)
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getLastEntryByCallerId(callerId)
            if (callLogs != null) {
                val dataToUpdate = dbRepository.callLogDao.getCallLogById(callLogs.id)
                val tempData = dataToUpdate?.copy(callNote = note, isSynced = false)
                if (tempData != null) {
                    dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                }
            }
            syncUpdateCallLogs()/*
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                this@CallStateDetectionService,
                                "Note saved successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
            */

            /*  runOnUiThread {
                  Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                  // finish()
              }*/
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@CallStateDetectionService, "Failed to save note!", Toast.LENGTH_SHORT
                ).show()
            }/*        runOnUiThread {
                        Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
                    }*/
        }
    }

    private suspend fun insertTasksOnCallLog(tasks: List<Tasks?>, callerId: String) {
        val tempTasks = arrayListOf<Tasks>()
        tasks.forEach {
            if (it != null && !it.tasks.isNullOrEmpty()) {
                tempTasks.add(it)
            }
        }
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getLastEntryByCallerId(callerId)
            if (callLogs != null) {
                val dataToUpdate = dbRepository.callLogDao.getCallLogById(callLogs.id)
                val tempData = dataToUpdate?.copy(tasks = tempTasks, isSynced = false)
                if (tempData != null) {
                    dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                }
            }
            syncUpdateCallLogs()
        }
    }

    private fun startCallerIdService(type: Int, phoneNo: String?) {
        var callerIdType = 0
        CoroutineScope(Dispatchers.IO).launch {
            callerIdType = dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdType!!
            handlerToStopForeground.removeCallbacksAndMessages(null)
            if (type == 0) {
                Log.e("kjhghgjgf", "startCallerIdService: gone to point")
                callLogHelper.insertRecentCallLogs {}
                delay(100)
                val getTempData = dbRepository.tempData.getCallerIdOptions()
                if (getTempData != null) {
                    if (getTempData.tempNotesMap.containsKey(phoneNo)) {
                        insertNoteOnCallLog(getTempData.tempNotesMap[phoneNo], phoneNo!!)
                        delay(100)
                        getTempData.tempNotesMap.remove(phoneNo)
                        dbRepository.tempData.insertOrUpdateCallerIdOptions(getTempData)
                    }
                    if (getTempData.tempTasksMap.containsKey(phoneNo)) {
                        getTempData.tempTasksMap[phoneNo]?.let {
                            if (phoneNo != null) {
                                insertTasksOnCallLog(it, phoneNo)
                            }
                        }
                        getTempData.tempTasksMap.remove(phoneNo)
                        dbRepository.tempData.insertOrUpdateCallerIdOptions(getTempData)
                    }
                }

                //MyApp.instance.preNotes.remove(phoneNo)
            }

            if (callerIdType == 1) {
                showCustomNotification(this@CallStateDetectionService, "$type", "$phoneNo")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = "your_channslel_id"
                    val channelName = "Your Chanlnel Namse"
                    val channelId2 = "caller_id_service"
                    val channelName2 = "CallerID Service"
                    val notificationId = 1 // Unique notification ID
                    val intent = Intent(this@CallStateDetectionService, CallerIdService::class.java)
                    intent.putExtra("callType", type)
                    intent.putExtra("phoneNo", phoneNo)
                    intent.putExtra("activeCalls", Gson().toJson(activeCalls))
                    intent.putExtra("activeCallNote", Gson().toJson(activeCallsNote))
                    intent.putExtra("activeCallTasks", Gson().toJson(activeTasks))
                    val pendingIntent = PendingIntent.getActivity(
                        this@CallStateDetectionService,
                        0,
                        intent,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            FLAG_MUTABLE // Use FLAG_MUTABLE for S+ (API level 31) or higher
                            FLAG_MUTABLE  // Use FLAG_MUTABLE for S+ (API level 31) or higher
                        } else {
                            PendingIntent.FLAG_UPDATE_CURRENT // Use FLAG_UPDATE_CURRENT for lower API levels
                        }
                    )
                    val notificationBuilder =
                        NotificationCompat.Builder(this@CallStateDetectionService, channelId)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Caller ID Service")
                            .setContentText("Running in the background")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent).setSilent(true).setOngoing(true)
                            .setAutoCancel(true).setOnlyAlertOnce(true).setTimeoutAfter(1)
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId2, channelName2, NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationManager.createNotificationChannel(channel)
                    }

                    try {
                        // Start the service in the foreground immediately
                        //     startForeground(3, notificationBuilder.build())
                        //ContextCompat.startForegroundService(this, intent)
                        Log.e("lkoimjuhnbygtf", "startCallerIdService: 299")
                        startService(intent)
                    } catch (e: RuntimeException) {
                        Log.e("kmjnhbg", "startCallerIdService: ${e.message}")
                    }
                } else {
                    startService(
                        Intent(
                            this@CallStateDetectionService, CallerIdService::class.java
                        )
                    )
                }
            }
        }
    }

    private inner class PhoneCallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                handlePhoneCallState(phoneNumber, state)
            }
        }
    }


    fun handlePhoneCallState(phoneNumber: String?, state: String?) {
        if (phoneNumber.isNullOrEmpty()) return
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastEventTime
        if (timeDifference >= debounceInterval) {
            val obtainMessage = Message()
            when (state) {
                "RINGING" -> {
                    serviceScope.launch {
                        if (isIncomingCallsEnabled()) {
                            obtainMessage.what = CALL_STATE_RINGING
                            obtainMessage.arg1 = 1
                            obtainMessage.obj = phoneNumber
                            rootHandler?.sendMessage(obtainMessage)
                        }
                        MyApp.instance.callLogHelper.insertRecentCallLogs {}
                    }
                    if (activeCalls.size >= 4) {
                        val lastEntry = activeCalls.entries.lastOrNull()
                        lastEntry?.key?.let {
                            activeCalls.remove(it)
                            activeCallsNote.remove(it)
                            activeTasks.remove(it)
                        }
                    }
                    // Add the new key-value pair to the map
                    activeCalls[phoneNumber] = 1
                    activeCallsNote[phoneNumber] = ""
                    activeTasks[phoneNumber] = emptyList()
                }

                "OFFHOOK" -> {
                    serviceScope.launch {
                        if (isOutgoingCallsEnabled()) {
                            obtainMessage.what = CALL_STATE_RINGING
                            obtainMessage.arg1 = 2
                            obtainMessage.obj = phoneNumber
                            rootHandler?.sendMessage(obtainMessage)
                        }
                        MyApp.instance.callLogHelper.insertRecentCallLogs {}
                    }
                    if (activeCalls.size >= 4) {
                        val lastEntry = activeCalls.entries.lastOrNull()
                        lastEntry?.key?.let {
                            activeCalls.remove(it)
                            activeCallsNote.remove(it)
                            activeTasks.remove(it)
                        }
                    }
                    // Add the new key-value pair to the map
                    activeCalls[phoneNumber] = 1
                    activeCallsNote[phoneNumber] = ""
                    activeTasks[phoneNumber] = emptyList()
                }

                "IDLE" -> {
                    serviceScope.launch {
                        if (isPostCallsEnabled()) {
                            obtainMessage.what = CALL_STATE_RINGING
                            obtainMessage.arg1 = ZERO
                            obtainMessage.obj = phoneNumber
                            rootHandler?.sendMessage(obtainMessage)
                        }
                        MyApp.instance.callLogHelper.insertRecentCallLogs {}
                    }
                    // activeCalls.remove(phoneNumber)
                }
            }

            lastEventTime = currentTime
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            SERVICE_STARTED -> {
                logger.logInfo("ServiceStartedAndHeartbeatScheduled")
                setAlarm()
                rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, TWENTY_SECONDS)
                return true
            }

            HEARTBEAT_INITIATED -> {
                logger.logInfo("Heartbeat initiated at ${getCurrentTime(System.currentTimeMillis())}")
                setAlarm()
                startSilentPlayback()
                rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, TWENTY_SECONDS)
                return true
            }

            PERIODIC_5_S -> {
                serviceScope.launch {
                    try {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastHeartbeatTime <= ONE_MINUTE_FIFTEEN_SECONDS || currentTime - lastPushHeartbeatTime <= ONE_MINUTE) {
                            logger.logInfo("FiveSecExecLog")
                        } else {
                            rootHandler?.sendEmptyMessage(INITIATING_MANUAL_WORK)
                        }
                        rootHandler?.removeMessages(PERIODIC_5_S)
                        rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, FIVE_SEC_DELAY)
                    } catch (e: Exception) {
                        // Remove any pending executions of message 43 (if any)
                        logger.logError("PERIODIC_5_S: ${e.message}")
                        logger.logError("after exception retrying for PERIODIC_5_S")
                        rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, FIVE_SEC_DELAY)
                    }
                }
                return true
            }

            INITIATING_MANUAL_WORK -> {
                val currentTime = System.currentTimeMillis()
                val duration = checkElapsedTimeOfHeartbeat(currentTime)
                logger.logInfo("Pushing Heartbeat at ${getCurrentTime(currentTime)}")
                lastPushHeartbeatTime = System.currentTimeMillis()
                startSilentPlayback(duration)
            }

            CALL_STATE_RINGING -> {
                if (!isServiceRunning(
                        this, CallerIdService::class.java
                    )
                ) stopAppCallerIdService(this)
                startCallerIdService(
                    msg.arg1, msg.obj.toString()
                )
            }

            CALL_STATE_OFFHOOK -> {
                if (!isServiceRunning(
                        this, CallerIdService::class.java
                    )
                ) stopAppCallerIdService(this)
                startCallerIdService(
                    msg.arg1, msg.obj.toString()
                )
            }
        }
        return false
    }


    private fun checkElapsedTimeOfHeartbeat(currentTime: Long): Long {
        // Check if lastHeartbeatTime is uninitialized or 0
        if (lastHeartbeatTime == 0L) {
            lastHeartbeatTime = currentTime // Set it to the current time
            logger.logWarning("First heartbeat detected. Initializing lastHeartbeatTime.")
            return 5000 // Return a default value to allow the first heartbeat
        }
        val elapsedTime = currentTime - lastHeartbeatTime
        val elapsedMinutes = elapsedTime / (60 * 1000) // Convert milliseconds to minutes
        logger.logWarning("Heartbeat delayed by $elapsedMinutes minutes")
        // Notification threshold (3 minutes) in milliseconds
        val iterator = notificationQueue.iterator()
        while (iterator.hasNext()) {
            val (notificationId, timestamp) = iterator.next()
            if (currentTime - timestamp > TWO_MINUTE) {
                NotificationManagerCompat.from(this).cancel(notificationId)
                iterator.remove() // Remove from the map after cancellation
                logger.logWarning("Notification $notificationId removed after exceeding threshold.")
            }
        }
        // Check if the delay is more than 15 minutes
        if (elapsedMinutes > 15) {
            cancelAllHeartbeatAlarms()  // Cancel existing alarms
        }
        return when {
            elapsedMinutes < 1 -> 5000

            elapsedMinutes < 2 -> 5000

            elapsedMinutes < 3 -> 5000

            elapsedMinutes < 4 -> 5000

            elapsedMinutes < 5 -> 5000

            elapsedMinutes < 6 -> 5000

            elapsedMinutes < 7 -> 6000

            elapsedMinutes < 8 -> 7000

            elapsedMinutes < 9 -> 7000

            elapsedMinutes < 10 -> 8000

            else -> 9000
        }
    }

    // Function to cancel all existing heartbeat alarms
    private fun cancelAllHeartbeatAlarms() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val heartbeatIntent = PendingIntent.getService(
            this,
            ZERO,
            Intent(ACTION_HEARTBEAT, null, this, CallStateDetectionService::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel the existing alarm
        alarmManager.cancel(heartbeatIntent)
        logger.logWarning("All heartbeat alarms canceled due to delay exceeding 15 minutes.")
        setAlarm()
        logger.logInfo("New heartbeat alarm scheduled.")
    }


    @SuppressLint("ScheduleExactAlarm")
    fun scheduleHeartbeat(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val heartbeatMsFor = ONE_MINUTE // Adjust as needed
        logger.logInfo("Scheduling heartbeat in 60 seconds...")

        if (heartbeatIntent == null) {
            logger.logError("scheduleHeartbeat: heartbeatIntent is null, cannot schedule alarm.")
            return
        }

        val currentApiLevel = Build.VERSION.SDK_INT

        if (currentApiLevel >= Build.VERSION_CODES.M) {
            // API 23 and above: use setExactAndAllowWhileIdle for better accuracy even in Doze mode
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + heartbeatMsFor,
                heartbeatIntent!!
            )
            logger.logInfo("Using setExactAndAllowWhileIdle for heartbeat scheduling.")
        } else if (currentApiLevel < Build.VERSION_CODES.KITKAT) {
            // API below 19: use set directly
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] =
                heartbeatIntent!!
        } else {
            // API 19 to 22: use setWindow for more efficient batching
            val windowMs = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + windowMs * 3,
                windowMs,
                heartbeatIntent!!
            )
        }
    }


    private fun setAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val exactAlarmPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
            //val useExactAlarmPermission = Manifest.permission.USE_EXACT_ALARM

            if (ContextCompat.checkSelfPermission(
                    this, exactAlarmPermission
                ) != PackageManager.PERMISSION_GRANTED

            /* ContextCompat.checkSelfPermission(
                 this,
                 useExactAlarmPermission
             ) != PackageManager.PERMISSION_GRANTED*/) {
                logger.logError("setAlarm: permission not granted")
                scheduleHeartbeat(this)/*                // Request the permissions
                                ActivityCompat.requestPermissions(
                                    applicationContext as Activity,
                                    arrayOf(exactAlarmPermission, useExactAlarmPermission),
                                    223
                                )*/

            } else {
                scheduleHeartbeat(this)
            }
        } else {
            scheduleHeartbeat(this)
        }
    }

    private inner class HandlerThread : Thread() {
        init {
            name = "McsHandler"
        }

        @SuppressLint("InvalidWakeLockTag")
        override fun run() {
            Looper.prepare()
            wakeLock = powerManager?.newWakeLock(1, "mcs")
            @SuppressLint("InvalidWakeLockTag") val unused = wakeLock
            wakeLock?.setReferenceCounted(false)
            synchronized(CallStateDetectionService::class.java) {
                rootHandler = Handler(Looper.myLooper()!!, this@CallStateDetectionService)
                val unused2 = rootHandler
                if (connectIntent != null) {
                    rootHandler!!.sendMessage(
                        rootHandler!!.obtainMessage(
                            SERVICE_STARTED, connectIntent
                        )
                    )
                    ServiceControlReceiver.completeWakefulIntent(connectIntent)
                }
            }
            Looper.loop()
        }
    }

    private fun getCurrentTime(time: Long = System.currentTimeMillis()): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = Date(time)
        return dateFormat.format(currentTime)
    }

    private fun triggerAlarm(callerId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            var contactName = ""
            val getReminder = dbRepository.reminder.getCallerIdOptions(callerId)
            val logData = dbRepository.callLogDao.getCallLogById(getReminder?.reminderId!!)
            val cachedName = contactHelper.getNameFromPhoneNumber(
                logData?.number ?: ""
            )
            if (cachedName.isEmpty() || cachedName == "Unknown") {
                var checkName: String? = ""
                checkName = dbRepository.contact.getContactByPhoneNumber(
                    logData?.number ?: ""
                )?.contact_title
                contactName = if (!checkName.isNullOrEmpty()) {
                    checkName
                } else "Unknown"
            } else {
                contactName = cachedName.ifEmpty { "Unknown" }

            }
            val contentText =
                if (!logData?.callNote.isNullOrBlank()) logData?.callNote else if (contactName == "Unknown" || contactName.isEmpty()) logData?.number else contactName
            if (getReminder.status) {
                NotificationHelper(this@CallStateDetectionService).showAlarmNotification(
                    "Reminder", contentText, logData?.id.toString(), getReminder.callerId
                )
                val alarmPlayer = AlarmPlayer(this@CallStateDetectionService)
                alarmPlayer.playAlarm()
                getReminder.status = false
                dbRepository.reminder.insertOrUpdateCallerIdOptions(getReminder)
            }
        }
    }

    companion object {
        private var wakeLock: PowerManager.WakeLock? = null
        private var rootHandler: Handler? = null
        private var handlerThread: HandlerThread? = null
        val isPersistentProcess2: Boolean
            get() {
                @SuppressLint("NewApi", "LocalSuppress") val processName =
                    Application.getProcessName()
                if (processName == null) {
                    Log.w("GmsPackageUtils", "Can't determine process name of current process")
                    return false
                }
                return processName.endsWith(":persistent")
            }
        private const val CHANNEL_DESCRIPTION = "Channel for displaying custom notifications"
    }
}
