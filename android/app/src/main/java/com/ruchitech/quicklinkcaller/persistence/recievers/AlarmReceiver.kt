package com.ruchitech.quicklinkcaller.persistence.recievers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.MainActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.CallStateHandleService
import com.ruchitech.quicklinkcaller.persistence.McsConstants.EXTRA_REASON
import com.ruchitech.quicklinkcaller.persistence.McsConstants.REMINDER
import com.ruchitech.quicklinkcaller.persistence.foreground_notification.ForegroundServiceContext
import com.ruchitech.quicklinkcaller.persistence.recievers.ServiceControlReceiver.startWakefulService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("kmjiuhyg", "Alarm triggered!")

        Toast.makeText(context, "${ intent?.getLongExtra("alarmID", 0)}", Toast.LENGTH_SHORT).show()
        // Handle actions when the alarm is received
        // For example, show a notification
        // You can extract data from the intent to customize behavior
        //  showNotification(context,"fljhd","fgdl;jgdfgj")
        val foregroundServiceContext: ForegroundServiceContext =
            ForegroundServiceContext(context)
        val putExtra = Intent(
            REMINDER,
            null,
            context,
            CallStateDetectionService::class.java
        ).putExtra("alarmID",  intent?.getLongExtra("alarmID", 0))
        startWakefulService(foregroundServiceContext, putExtra)

    }




    private fun showNotification(context: Context?, title: String, message: String) {
        context?.let {
            val channelId = "phone_call_channel"
            createNotificationChannel(it, channelId)

            // PendingIntent for opening the app when notification is tapped
            val notificationIntent = Intent(it, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                it,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create the notification
            val notification = NotificationCompat.Builder(it, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your app's icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Open the app on notification click
                .setAutoCancel(true) // Auto-dismiss when tapped
                .build()

            // Show the notification
            NotificationManagerCompat.from(it).notify(1, notification)
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Phone Call Notifications"
            val descriptionText = "Shows notifications for phone call events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
