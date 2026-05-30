package com.ruchitech.quicklinkcaller.persistence

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.contactutills.parsers.PhoneNumberParser
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.Logger
import com.ruchitech.quicklinkcaller.helper.isServiceRunning
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.persistence.McsConstants.CALL_STATE_OFFHOOK
import com.ruchitech.quicklinkcaller.persistence.McsConstants.CALL_STATE_RINGING
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ZERO
import com.ruchitech.quicklinkcaller.persistence.foreground_notification.ForegroundServiceContext
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_ADD_NOTE
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_NOTIFICATION_REPLY
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_REMINDER
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver.Companion.ACTION_SHARE_CONTACT
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.NotificationsQueue
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.CallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.stopAppCallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject


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

@AndroidEntryPoint
class CallStateHandleService : Service(), Handler.Callback {

    private val CHANNEL_ID = "notification_for_calls"
    private val CHANNEL_NAME = "Call notification"
    private val NOTIFICATION_ID = 1

    private var connectIntent: Intent? = null
    private var alarmManager: AlarmManager? = null
    private lateinit var logger: Logger
    private val debounceInterval = 1000L // 1 second
    private var lastEventTime: Long = 0
    private val handlerToStopForeground: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val appPreferences by lazy { AppPreferences(applicationContext) }
    private val telephonyManager: TelephonyManager by lazy {
        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

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
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    @Inject
    lateinit var contactHelper: ContactHelper

    @Inject
    lateinit var appPreference: AppPreference

    @Inject
    lateinit var phoneNumberParser: PhoneNumberParser
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
        val logFile = File(getExternalFilesDir(null), "app_log.txt")
        logger = Logger("InsetsController024", logFile)
        val callLogUri = CallLog.Calls.CONTENT_URI
        val callLogObserver = CallLogObserver(Handler(Looper.getMainLooper()))
        contentResolver.registerContentObserver(callLogUri, true, callLogObserver)

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

        ForegroundServiceContext.completeForegroundService(
            this, intent, "CallStateDetectionService2"
        )
        // Extract the original intent from the extra
        val originalIntent: Intent? = intent?.getParcelableExtra(McsConstants.EXTRA_REASON)

        originalIntent?.let {
            val phoneNumber = it.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val state = it.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (!phoneNumber.isNullOrEmpty()) {
                val parsedMap = phoneNumberParser.formatPhoneNumber(phoneNumber)

                // Call handlePhoneCallState with the extracted data
                handlePhoneCallState(parsedMap, state)
            }
        }



        stopForeground(true)

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
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
        val notificationId =
            callingNumber.hashCode() // Use phone number hashcode as notification ID
        CoroutineScope(Dispatchers.IO).launch {
            var checkName: String? = ""
            val contactDetails = contactHelper.getNameFromPhoneNumber(callingNumber)
            if (contactDetails.isEmpty() || contactDetails == "Unknown") {
                checkName =
                    dbRepository.contact.getContactByPhoneNumber(callingNumber)?.contact_title
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
            // Show the notification
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
            val currentTime = Date().time


            val notificationsList = dbRepository.tempData.getExpiredNotifications(currentTime)
            notificationsList.forEach { notifications ->
                NotificationManagerCompat.from(this@CallStateHandleService)
                    .cancel(notifications.notificationId)
            }

            notificationQueue[notificationId] = currentTime
            val tempNotification = NotificationsQueue(
                notificationId = notificationId,
                createdTime = currentTime,
                type = 1
            )
            dbRepository.tempData.insertOrUpdateNotification(tempNotification)
            dbRepository.tempData.cancelExpiredNotifications(currentTime)
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
                                this@CallStateHandleService,
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
                    this@CallStateHandleService, "Failed to save note!", Toast.LENGTH_SHORT
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

    private fun handleUiUserActionsByNotificationOrPopup(type: Int, phoneNo: String?) {
        var callerIdType = 0
        CoroutineScope(Dispatchers.IO).launch {
            callerIdType = dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdType!!
            handlerToStopForeground.removeCallbacksAndMessages(null)
            if (type == 0) {
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

            if (callerIdType == 2) return@launch

            if (callerIdType == 1) {
                showCustomNotification(this@CallStateHandleService, "$type", "$phoneNo")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = "your_channslel_id"
                    val channelName = "Your Chanlnel Namse"
                    val channelId2 = "caller_id_service"
                    val channelName2 = "CallerID Service"
                    val notificationId = 1 // Unique notification ID
                    val intent = Intent(this@CallStateHandleService, CallerIdService::class.java)
                    intent.putExtra("callType", type)
                    intent.putExtra("phoneNo", phoneNo)
                    intent.putExtra("activeCalls", Gson().toJson(activeCalls))
                    intent.putExtra("activeCallNote", Gson().toJson(activeCallsNote))
                    intent.putExtra("activeCallTasks", Gson().toJson(activeTasks))
                    val pendingIntent = PendingIntent.getActivity(
                        this@CallStateHandleService,
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
                        NotificationCompat.Builder(this@CallStateHandleService, channelId)
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
                            this@CallStateHandleService, CallerIdService::class.java
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
                if (!phoneNumber.isNullOrEmpty()) {
                    val parsedNumber = phoneNumberParser.formatPhoneNumber(phoneNumber)
                    handlePhoneCallState(parsedNumber, state)
                }

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
                            if (!isServiceRunning(
                                    this@CallStateHandleService,
                                    CallerIdService::class.java
                                )
                            ) stopAppCallerIdService(this@CallStateHandleService)
                            handleUiUserActionsByNotificationOrPopup(
                                1, phoneNumber
                            )
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
                            if (!isServiceRunning(
                                    this@CallStateHandleService, CallerIdService::class.java
                                )
                            ) stopAppCallerIdService(this@CallStateHandleService)
                            handleUiUserActionsByNotificationOrPopup(2, phoneNumber)
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
                            if (!isServiceRunning(
                                    this@CallStateHandleService, CallerIdService::class.java
                                )
                            ) stopAppCallerIdService(this@CallStateHandleService)
                            handleUiUserActionsByNotificationOrPopup(
                                0, phoneNumber
                            )
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
            CALL_STATE_RINGING -> {
                if (!isServiceRunning(
                        this, CallerIdService::class.java
                    )
                ) stopAppCallerIdService(this)
                handleUiUserActionsByNotificationOrPopup(
                    msg.arg1, msg.obj.toString()
                )
            }

            CALL_STATE_OFFHOOK -> {
                if (!isServiceRunning(
                        this, CallerIdService::class.java
                    )
                ) stopAppCallerIdService(this)
                handleUiUserActionsByNotificationOrPopup(
                    msg.arg1, msg.obj.toString()
                )
            }
        }
        return false
    }

    private inner class HandlerThread : Thread() {
        init {
            name = "McsHandler"
        }

        @SuppressLint("InvalidWakeLockTag")
        override fun run() {
            Looper.prepare()
            @SuppressLint("InvalidWakeLockTag") val unused = wakeLockthis@ CallStateHandleService
            Looper.loop()
        }
    }

    companion object {
        private const val CHANNEL_DESCRIPTION = "Channel for displaying custom notifications"
    }
}
