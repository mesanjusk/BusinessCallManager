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

    private val phonePattern = Regex("^[+\\d][\\d\\s\\-()]{6,}$")

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in whatsappPackages) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString("android.title")?.trim() ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        // Skip group chats (title ends with @ or contains brackets typical of groups)
        if (title.contains("@") || title.startsWith("[")) return
        // Skip system / summary notifications
        if (title.equals("WhatsApp", ignoreCase = true)) return

        val isCallNotif = sbn.notification.category == android.app.Notification.CATEGORY_CALL ||
            text.contains("incoming voice call", ignoreCase = true) ||
            text.contains("incoming video call", ignoreCase = true) ||
            text.contains("missed voice call", ignoreCase = true) ||
            text.contains("missed video call", ignoreCase = true)

        val isLikelyPhone = phonePattern.matches(title)

        // phone = real number when available, otherwise sanitize the name as a unique key
        val phone: String
        val name: String?
        if (isLikelyPhone) {
            phone = title.replace("[\\s\\-()]".toRegex(), "")
            name = null
        } else {
            // Named contact — use a stable, sanitized key as the phone identifier
            phone = "wa_${title.replace("[^a-zA-Z0-9]".toRegex(), "_").take(40)}"
            name = title
        }

        val source = if (isCallNotif) "whatsapp_call" else "whatsapp"

        CoroutineScope(Dispatchers.IO).launch {
            val db = try { MyApp.instance.dbRepository } catch (e: Exception) { return@launch }
            val prefs = try { MyApp.instance.appPreference } catch (e: Exception) { return@launch }
            val userUuid = prefs.userId ?: return@launch

            val existingLead = db.leadDao.getLeadByPhone(phone)
            if (existingLead != null) {
                // Update timestamp so it appears in recent activity
                db.leadDao.updateLead(existingLead.copy(updated_at = System.currentTimeMillis()))
                return@launch
            }

            val lead = Lead(
                lead_uuid = UUID.randomUUID().toString(),
                user_uuid = userUuid,
                phone = phone,
                name = name,
                source = source,
                status = "New",
                notes = "[]",
                call_refs = "[]",
                isSynced = false
            )
            db.leadDao.insertLead(lead)
            showLeadNotification(name ?: phone, text, lead.lead_uuid)
        }
    }

    private fun showLeadNotification(displayName: String, preview: String, leadUuid: String) {
        val channelId = "whatsapp_leads_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WhatsApp Leads", NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val openIntent = Intent(this, PostCallActivity::class.java).apply {
            action = NotificationReceiver.ACTION_ADD_LEAD
            putExtra("number", displayName)
            putExtra("name", displayName)
            putExtra("source", "whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, displayName.hashCode() + 200, openIntent,
            FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val displayPreview = if (preview.length > 50) "${preview.take(50)}…" else preview
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("New WhatsApp Activity: $displayName")
            .setContentText(displayPreview)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(displayName.hashCode() + 200, notif)
        }
    }
}
