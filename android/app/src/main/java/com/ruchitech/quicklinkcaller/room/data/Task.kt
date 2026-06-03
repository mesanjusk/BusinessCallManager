package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks", indices = [Index("assigned_to_uuid"), Index("status")])
data class Task(
    @PrimaryKey val task_uuid: String,
    val created_by_uuid: String,
    val assigned_to_uuid: String,
    val title: String,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val business_uuid: String? = null,
    val lead_uuid: String? = null,
    val call_ref: String? = null,
    val description: String? = null,
    val due_date: Long? = null,
    val priority: String = "Medium",
    val status: String = "Pending",
    val isSynced: Boolean = false
)
