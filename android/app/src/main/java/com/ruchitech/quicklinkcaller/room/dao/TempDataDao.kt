package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.room.data.NotificationsQueue
import com.ruchitech.quicklinkcaller.room.data.TempDataEntity

@Dao
interface TempDataDao {

    @Query("SELECT * FROM temp_data WHERE id = 1 LIMIT 1")
    suspend fun getCallerIdOptions(): TempDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallerIdOptions(entity: TempDataEntity)

    // Insert or replace a notification based on unique notificationId
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNotification(entity: NotificationsQueue)

    // Delete a notification using its notificationId
    @Query("DELETE FROM notifications_queue WHERE notificationId = :notificationId")
    suspend fun deleteNotificationById(notificationId: Int)

    // Get a list of all notifications
    @Query("SELECT * FROM notifications_queue")
    suspend fun getAllNotifications(): List<NotificationsQueue>

    // Get a list of expired notifications
    @Query("SELECT * FROM notifications_queue WHERE createdTime < (:currentTime - 120000)")
    suspend fun getExpiredNotifications(currentTime: Long): List<NotificationsQueue>

    // Cancel notifications if their createdTime is over 2 minutes and remove them from the table
    @Query("DELETE FROM notifications_queue WHERE createdTime < (:currentTime - 120000)")
    suspend fun cancelExpiredNotifications(currentTime: Long)
}
