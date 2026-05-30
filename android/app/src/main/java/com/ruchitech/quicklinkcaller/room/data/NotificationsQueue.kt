package com.ruchitech.quicklinkcaller.room.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications_queue")
data class NotificationsQueue(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id") val id: Long?=null,
    @ColumnInfo("notificationId") val notificationId: Int,
    @ColumnInfo("createdTime") val createdTime: Long,
    @ColumnInfo("type") val type: Int, //1 for call logs
)
