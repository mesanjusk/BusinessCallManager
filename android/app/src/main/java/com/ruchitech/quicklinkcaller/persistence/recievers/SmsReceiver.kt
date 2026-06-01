package com.ruchitech.quicklinkcaller.persistence.recievers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.room.data.Lead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody }

        CoroutineScope(Dispatchers.IO).launch {
            val db = MyApp.instance.dbRepository
            val prefs = MyApp.instance.appPreference
            val userUuid = prefs.userId ?: return@launch

            val existingLead = db.leadDao.getLeadByPhone(sender)
            if (existingLead != null) return@launch

            val lead = Lead(
                lead_uuid = UUID.randomUUID().toString(),
                user_uuid = userUuid,
                phone = sender,
                source = "sms",
                status = "New",
                notes = "[]",
                call_refs = "[]",
                isSynced = false
            )
            db.leadDao.insertLead(lead)

            showLeadNotification(context, sender, body, lead.lead_uuid)
        }
    }

    private fun showLeadNotification(context: Context, phone: String, preview: String, leadUuid: String) {
        val channelId = "sms_leads_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SMS Leads", NotificationManager.IMPORTANCE_DEFAULT)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val openIntent = Intent(context, PostCallActivity::class.java).apply {
            action = NotificationReceiver.ACTION_ADD_LEAD
            putExtra("number", phone)
            putExtra("name", "Unknown")
            putExtra("source", "sms")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, phone.hashCode(), openIntent,
            FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val displayPreview = if (preview.length > 50) "${preview.take(50)}…" else preview
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("New SMS Lead: $phone")
            .setContentText(displayPreview)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_add, "View Lead", pendingIntent)
            .build()

        if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(phone.hashCode(), notif)
        }
    }
}
