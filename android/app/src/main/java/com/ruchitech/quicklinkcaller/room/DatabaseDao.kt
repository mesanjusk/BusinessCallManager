package com.ruchitech.quicklinkcaller.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruchitech.quicklinkcaller.room.dao.CallLogDao
import com.ruchitech.quicklinkcaller.room.dao.CallerIdOptionsDao
import com.ruchitech.quicklinkcaller.room.dao.ContactDao
import com.ruchitech.quicklinkcaller.room.dao.DataDao
import com.ruchitech.quicklinkcaller.room.dao.ReminderDao
import com.ruchitech.quicklinkcaller.room.dao.TempDataDao
import com.ruchitech.quicklinkcaller.room.dao.TimestampDao
import com.ruchitech.quicklinkcaller.room.dao.LeadDao
import com.ruchitech.quicklinkcaller.room.dao.TaskDao
import com.ruchitech.quicklinkcaller.room.dao.BusinessDao
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.NotificationsQueue
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.TempDataEntity
import com.ruchitech.quicklinkcaller.room.data.Timestamp
import com.ruchitech.quicklinkcaller.room.data.User
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.room.data.Task
import com.ruchitech.quicklinkcaller.room.data.Business
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContact


@Database(
    entities = [
        User::class,
        CallLogs::class,
        CallLogDetails::class,
        Contact::class,
        Timestamp::class,
        CallerIdOptionsEntity::class,
        Reminders::class,
        DeleteSecondaryContact::class,
        TempDataEntity::class,
        NotificationsQueue::class,
        Lead::class,
        Task::class,
        Business::class,
    ],
    version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DatabaseDao : RoomDatabase() {
    abstract fun dataDao(): DataDao
    abstract fun callLogs(): CallLogDao
    abstract fun contact(): ContactDao
    abstract fun callerIDOptions(): CallerIdOptionsDao
    abstract fun timestampDao(): TimestampDao
    abstract fun reminders(): ReminderDao
    abstract fun tempData(): TempDataDao
    abstract fun leadDao(): LeadDao
    abstract fun taskDao(): TaskDao
    abstract fun businessDao(): BusinessDao
}