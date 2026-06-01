package com.ruchitech.quicklinkcaller.persistence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver
import com.ruchitech.quicklinkcaller.room.data.Lead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class WhatsAppNotificationListener : NotificationListenerService() {

    private val whatsappPackages = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in whatsappPackages) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        // Detect WhatsApp calls: category is "call" or text contains call keywords
        val isCallNotif = sbn.notification.category == android.app.Notification.CATEGORY_CALL ||
            text.contains("call", ignoreCase = true)

        // For messages: title is phone number format
        // For calls: title may be contact name — capture both
        val phonePattern = Regex("^[+\\d][\\d\\s\\-()]{6,}$")
        val isLikelyPhone = phonePattern.matches(title.trim())

        if (!isLikelyPhone && !isCallNotif) return

        val phone = if (isLikelyPhone) title.trim().replace("[\\s\\-()]".toRegex(), "") else title.trim()
        val source = if (isCallNotif) "whatsapp_call" else "whatsapp"

        CoroutineScope(Dispatchers.IO).launch {
            val db = try { MyApp.instance.dbRepository } catch (e: Exception) { return@launch }
            val prefs = try { MyApp.instance.appPreference } catch (e: Exception) { return@launch }
            val userUuid = prefs.userId ?: return@launch

            val existingLead = db.leadDao.getLeadByPhone(phone)
            if (existingLead != null) return@launch

            val lead = Lead(
                lead_uuid = UUID.randomUUID().toString(),
                user_uuid = userUuid,
                phone = phone,
                source = source,
                status = "New",
                notes = "[]",
                call_refs = "[]",
                isSynced = false
            )
            db.leadDao.insertLead(lead)
            showLeadNotification(phone, text, lead.lead_uuid)
        }
    }

    private fun showLeadNotification(phone: String, preview: String, leadUuid: String) {
        val channelId = "whatsapp_leads_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WhatsApp Leads", NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val openIntent = Intent(this, PostCallActivity::class.java).apply {
            action = NotificationReceiver.ACTION_ADD_LEAD
            putExtra("number", phone)
            putExtra("name", "Unknown")
            putExtra("source", "whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, phone.hashCode() + 200, openIntent,
            FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val displayPreview = if (preview.length > 50) "${preview.take(50)}…" else preview
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("New WhatsApp Lead: $phone")
            .setContentText(displayPreview)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_add, "View Lead", pendingIntent)
            .build()

        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(phone.hashCode() + 200, notif)
        }
    }
}
