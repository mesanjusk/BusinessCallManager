package com.ruchitech.quicklinkcaller.persistence.recievers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.NotificationHelper
import com.ruchitech.quicklinkcaller.helper.copyToClipboard
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.helper.shareContact
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.CallerIdService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_WHATSAPP = "ACTION_WHATSAPP"
        const val ACTION_COPY_NUMBER = "ACTION_COPY_NUMBER"
        const val ACTION_SHARE_NUMBER = "ACTION_SHARE_NUMBER"
        const val ACTION_CALL = "ACTION_CALL"
        const val ACTION_SERVICE = "ACTION_SERVICE"
        const val ACTION_NOTIFICATION_REPLY = "ACTION_NOTIFICATION_REPLY"
        const val ACTION_ADD_NOTE = "ACTION_ADD_NOTE"
        const val ACTION_TEXT_MSG = "ACTION_TEXT_MSG"
        const val ACTION_SHARE_CONTACT = "ACTION_SHARE_CONTACT"
        const val ACTION_REMINDER = "ACTION_REMINDER"
        const val ACTION_SAVE_CONTACT = "ACTION_SAVE_CONTACT"
        const val ACTION_ADD_LEAD = "ACTION_ADD_LEAD"

    }

    @Inject
    lateinit var callLogHelper: CallLogHelper

    @Inject
    lateinit var dbRepository: DbRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {

            ACTION_COPY_NUMBER -> {
                val number = intent.getStringExtra("number")
                context?.copyToClipboard(number!!)
            }

            ACTION_SHARE_NUMBER -> {
                val number = intent.getStringExtra("number")
                val contactName = intent.getStringExtra("contactName")
                context?.shareContact(contactName ?: "", number!!)
            }

            ACTION_CALL -> {
                val number = intent.getStringExtra("number")
                if (number != null) {
                    context?.makePhoneCall(number)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    context?.let {
                        val notificationHelper = NotificationHelper(it)
                        notificationHelper.cancelNotification()
                        // Add additional actions as needed
                    }
                }, 1500)
            }

            ACTION_TEXT_MSG -> {
                val number = intent.getStringExtra("number")
                if (number != null) {
                    context?.sendTextMessage(number)
                }
            }

            ACTION_WHATSAPP -> {
                // Handle the cancel action (e.g., stop the alarm)
                val number = intent.getStringExtra("number")
                if (number != null) {
                    context?.openWhatsapp(number)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    context?.let {
                        val notificationHelper = NotificationHelper(it)
                        notificationHelper.cancelNotification()
                        // Add additional actions as needed
                    }
                }, 100)
            }

            ACTION_SERVICE -> {
                val number = intent.getStringExtra("number")
                val type = intent.getStringExtra("type")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = "caller_id_service"
                    val channelName = "CallerID Service"
                    val notificationId = 1 // Unique notification ID
                    val intent = Intent(context, CallerIdService::class.java)
                    intent.putExtra("callType", type)
                    intent.putExtra("phoneNo", number)
                    intent.putExtra("activeCalls", "[]")
                    intent.putExtra("activeCallNote", "[]")
                    intent.putExtra("activeCallTasks", "[]")
                    val pendingIntent =
                        PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PendingIntent.FLAG_MUTABLE  // Use FLAG_MUTABLE for S+ (API level 31) or higher
                            } else {
                                PendingIntent.FLAG_UPDATE_CURRENT // Use FLAG_UPDATE_CURRENT for lower API levels
                            }
                        )
                    val notificationBuilder = NotificationCompat.Builder(context!!, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Caller ID Service")
                        .setContentText("Running in the background")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setSilent(true)
                        .setOngoing(true)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setTimeoutAfter(1)
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            channelName,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationManager.createNotificationChannel(channel)
                    }

                    try {
                        // Start the service in the foreground immediately
                        //     startForeground(3, notificationBuilder.build())
                        //ContextCompat.startForegroundService(this, intent)
                        context.startService(intent)
                    } catch (e: RuntimeException) {
                    }
                } else {
                    context?.startService(Intent(context, CallerIdService::class.java))
                }
            }

            ACTION_NOTIFICATION_REPLY -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                if (remoteInput != null) {
                    val replyText = remoteInput.getCharSequence("key_text_reply").toString()
                    val callType = intent?.getStringExtra("type")
                    val number = intent?.getStringExtra("number")
                    CoroutineScope(Dispatchers.IO).launch {
                        insertNoteOnCallLog(replyText, number!!, context)
                    }

                    val notificationManager =
                        context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationId = intent?.getIntExtra("notificationId", 0)
                    if (notificationId != null) {
                        Handler().postDelayed({
                            notificationManager.cancel(notificationId)
                        }, 2000)
                    }
                }
            }

            else -> {
                val notificationManager =
                    context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationId = intent?.getIntExtra("notificationId", 0)
                context.startActivity(Intent(context, PostCallActivity::class.java).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    private suspend fun insertNoteOnCallLog(note: String?, callerId: String, context: Context?) {
        callLogHelper.insertRecentCallLogs {}
        delay(100)
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getLastEntryByCallerId(callerId)
            if (callLogs != null) {
                val dataToUpdate =
                    dbRepository.callLogDao.getCallLogById(callLogs.id)
                val tempData = dataToUpdate?.copy(callNote = note, isSynced = false)
                if (tempData != null) {
                    dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                }
            }
            syncUpdateCallLogs()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Note saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            }

            /*  runOnUiThread {
                  Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                  // finish()
              }*/
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Failed to save note!", Toast.LENGTH_SHORT)
                    .show()
            }
            /*        runOnUiThread {
                        Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
                    }*/
        }
    }

}
