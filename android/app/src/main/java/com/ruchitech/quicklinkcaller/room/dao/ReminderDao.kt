package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.data.CallLogWithReminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE reminderId ==:id LIMIT 1")
    suspend fun getCallerIdOptions(id: Long): Reminders?

    @Query("SELECT * FROM reminders WHERE reminderId ==:number LIMIT 1")
    suspend fun getReminderByCallerId(number: Long): Reminders?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallerIdOptions(reminders: Reminders)

    @Query("SELECT cld.*, rem.* FROM Call_log_details cld LEFT JOIN reminders rem ON cld.callerId = rem.callerId WHERE cld.callNote IS NOT NULL")
    suspend fun getCallLogsWithReminders(): List<CallLogWithReminder>
/*    @Query("SELECT cld.*, rem.* FROM Call_log_details cld LEFT JOIN reminders rem ON cld.callerId = rem.callerId WHERE cld.callNote IS NOT NULL AND cld.callNote != '' ORDER BY cld.date DESC LIMIT :pageSize OFFSET :offset")
    suspend fun getCallLogsWithRemindersPaged(pageSize: Int, offset: Int): List<CallLogWithReminder>*/

    @Query("""
    SELECT cld.*, rem.* 
    FROM Call_log_details cld 
    LEFT JOIN reminders rem ON cld.id = rem.reminderId 
    WHERE (cld.callNote IS NOT NULL AND cld.callNote != '') 
          OR cld.tasks != '[]' 
          OR (rem.status = 1 AND rem.reminderId IS NOT NULL)
    ORDER BY cld.date DESC 
    LIMIT :pageSize OFFSET :offset
""")
    suspend fun getCallLogsWithRemindersPaged(pageSize: Int, offset: Int): List<CallLogWithReminder>


    @Query("SELECT * FROM reminders  WHERE status=1")
    suspend fun getAllReminders(): List<Reminders>

}
