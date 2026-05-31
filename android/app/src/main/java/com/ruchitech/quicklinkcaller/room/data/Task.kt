package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks", indices = [Index("assigned_to_uuid"), Index("status")])
data class Task(
    @PrimaryKey val task_uuid: String,
    val business_uuid: String? = null,
    val created_by_uuid: String,
    val assigned_to_uuid: String,
    val lead_uuid: String? = null,
    val call_ref: String? = null,
    val title: String,
    val description: String? = null,
    val due_date: Long? = null,
    val priority: String = "Medium", // Low|Medium|High
    val status: String = "Pending", // Pending|In Progress|Done|Overdue
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
