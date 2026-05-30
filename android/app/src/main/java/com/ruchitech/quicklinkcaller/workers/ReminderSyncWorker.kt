package com.ruchitech.quicklinkcaller.workers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Reminders

class ReminderSyncWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = MyApp.instance.dbRepository
        val list = db.reminder.getAllReminders()
        list.forEach {
            scheduleReminder(it, context, db)
        }

        return Result.success()
    }

    @SuppressLint("ScheduleExactAlarm")
    suspend fun scheduleReminder(data: Reminders, context: Context, dbRepository: DbRepository) {
        context.cancelExistingReminder(data.reminderId.toString())
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        dbRepository.reminder.insertOrUpdateCallerIdOptions(data)

        val intent =
            Intent(McsConstants.REMINDER, null, context, CallStateDetectionService::class.java)

        intent.putExtra("alarmID", data.reminderId)

        val reminderPendingIntent = PendingIntent.getService(
            context, data.reminderId.toInt(),  // Use reminderId as the requestCode to identify the PendingIntent
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val heartbeatMsFor = McsConstants.ONE_MINUTE
        val i5 = Build.VERSION.SDK_INT
        if (i5 >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                data.timeInMillis,
                reminderPendingIntent!!
            )
        } else if (i5 < 19) {
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] =
                reminderPendingIntent!!
        } else {
            val i6 = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + i6 * 3,
                i6,
                reminderPendingIntent!!
            )
        }
    }
}
