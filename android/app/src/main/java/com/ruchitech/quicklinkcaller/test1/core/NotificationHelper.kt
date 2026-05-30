package com.ruchitech.quicklinkcaller.test1.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.MainActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.test1.data.ContactImageRepository
import com.ruchitech.quicklinkcaller.test1.data.ContactsRepository
import com.ruchitech.quicklinkcaller.test1.ongoingCall.CallActivity


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
private const val INCOMING_CALL_NOTIFICATION_ID = 123
private const val ONGOING_CALL_NOTIFICATION_ID = 124

class NotificationHelper(private val context: Context) {
    private val callChannelId = "AMADZ_CALL_NOTIFICATION_ID"
    private val callChannelName = "AMADZ_CALL_NOTIFICATION"
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)
    private val contactsRepository = ContactsRepository(context)
    private val contactImageRepository = ContactImageRepository(context)
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                callChannelId, callChannelName, NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Incoming call notification"
            val ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                this.context, RingtoneManager.TYPE_RINGTONE
            )

            channel.setSound(
                ringtoneUri,
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            )
            val mgr = NotificationManagerCompat.from(context)
            mgr.createNotificationChannel(channel)
        }


    }

    fun displayIncomingCallNotification(phone: String) {

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClass(context, CallActivity::class.java)
        intent.putExtra("phone", phone)
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, callChannelId)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setContentIntent(pendingIntent)
        builder.setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.profile_pic)
        builder.setLargeIcon(largeIcon)
        val contactData = contactsRepository.getContactData(phone)
        builder.setContentText(phone)
        if (contactData != null) {
            builder.setContentText(contactData.name)
            builder.setSubText(contactData.companyName)
            val contactPic = contactImageRepository.loadContactImage(contactData.image)
            if (contactPic != null) {
                builder.setLargeIcon(contactPic)
            }
        }
        builder.setFullScreenIntent(pendingIntent, true)
        builder.setSmallIcon(R.drawable.ic_app_icon)
        builder.setContentTitle("Incoming Call")

        val acceptPendingIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, CallAcceptService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declinePendingIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, CallDeclineService::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        builder.addAction(R.drawable.baseline_check_24, "Accept", acceptPendingIntent)
        builder.addAction(R.drawable.outline_clear_24, "Decline", declinePendingIntent)
        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, builder.build())
    }

    fun displayOngoingCallNotification(phone: String) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
            setClass(context, CallActivity::class.java)
            putExtra("phone", phone)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, callChannelId).setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW) // ⬅️ Keep it subtle, just in status bar
            .setContentIntent(pendingIntent).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_app_icon).setContentTitle("Ongoing Call")
            .setContentText(phone)

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.profile_pic)
        builder.setLargeIcon(largeIcon)

        val contactData = contactsRepository.getContactData(phone)
        if (contactData != null) {
            builder.setContentText(contactData.name)
            builder.setSubText(contactData.companyName)
            contactImageRepository.loadContactImage(contactData.image)?.let {
                builder.setLargeIcon(it)
            }
        }

        notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, builder.build())
    }

    fun cancelIncommingCallNotification() {
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }

    fun cancelOngoingCallNotification() {
        notificationManager.cancel(ONGOING_CALL_NOTIFICATION_ID)
    }

    fun playCallRingTone() {
        stopCallRingTone()

        val ringtoneUri =
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)

        try {
            val afd = context.contentResolver.openAssetFileDescriptor(ringtoneUri, "r")
            afd?.let {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    isLooping = true
                    prepare()
                    start()
                }
                afd.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showMissedCallNotification(phone: String) {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setClass(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, callChannelId)
        builder.setContentIntent(pendingIntent)
        val contactData = contactsRepository.getContactData(phone)
        builder.setContentText(phone)
        builder.setAutoCancel(true)
        if (contactData != null) {
            builder.setContentText(contactData.name)
            builder.setSubText(contactData.companyName)
            val contactPic = contactImageRepository.loadContactImage(contactData.image)
            if (contactPic != null) {
                builder.setLargeIcon(contactPic)
            }
        }
        builder.setSmallIcon(R.drawable.app_logo_short_notification)
        builder.setContentTitle("Missed Call")
        notificationManager.notify(1233, builder.build())
    }

    fun stopCallRingTone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}