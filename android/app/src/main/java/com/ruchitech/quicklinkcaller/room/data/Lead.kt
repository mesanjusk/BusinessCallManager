package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "leads", indices = [Index("phone"), Index("status")])
data class Lead(
    @PrimaryKey val lead_uuid: String,
    val user_uuid: String,
    val phone: String,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val business_uuid: String? = null,
    val name: String? = null,
    val source: String = "call",
    val status: String = "New",
    val notes: String = "[]",
    val next_follow_up: Long? = null,
    val call_refs: String = "[]",
    val isSynced: Boolean = false
)
