package com.ruchitech.quicklinkcaller.persistence.recievers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.MainActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.CallStateHandleService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ACTION_HEARTBEAT
import com.ruchitech.quicklinkcaller.persistence.foreground_notification.ForegroundServiceContext
import com.ruchitech.quicklinkcaller.persistence.recievers.ServiceControlReceiver.startWakefulService


class LastCallProcessor : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        val foregroundServiceContext: ForegroundServiceContext =
            ForegroundServiceContext(context)
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (incomingNumber.isNullOrEmpty()) return
                Log.d("PhoneCallReceiver", "Incoming call from: $incomingNumber")
                // Show notification for incoming call
               val putExtra = Intent(
                    ACTION_HEARTBEAT,
                    null,
                    context,
                    CallStateHandleService::class.java
                ).putExtra(McsConstants.EXTRA_REASON, intent)
                startWakefulService(foregroundServiceContext, putExtra)
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Show notification for ongoing call
                // Show notification for call ended
                Log.d("PhoneCallReceiver", "Call is ongoing ")
                val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (incomingNumber.isNullOrEmpty()) return
                val putExtra = Intent(
                    ACTION_HEARTBEAT,
                    null,
                    context,
                    CallStateHandleService::class.java
                ).putExtra(McsConstants.EXTRA_REASON, intent)
                startWakefulService(foregroundServiceContext, putExtra)

            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d("PhoneCallReceiver", "Call ended or no calls")
                // Show notification for call ended
                val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (incomingNumber.isNullOrEmpty()) return
                val putExtra = Intent(
                    ACTION_HEARTBEAT,
                    null,
                    context,
                    CallStateHandleService::class.java
                ).putExtra(McsConstants.EXTRA_REASON, intent)
                startWakefulService(foregroundServiceContext, putExtra)
            }
        }
    }
}
