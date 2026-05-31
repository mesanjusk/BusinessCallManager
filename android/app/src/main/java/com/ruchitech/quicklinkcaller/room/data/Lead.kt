package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "leads", indices = [Index("phone"), Index("status")])
data class Lead(
    @PrimaryKey val lead_uuid: String,
    val user_uuid: String,
    val business_uuid: String? = null,
    val phone: String,
    val name: String? = null,
    val source: String = "call", // "call" | "manual"
    val status: String = "New", // New|Contacted|Interested|Negotiation|Won|Lost
    val notes: String = "[]", // JSON string of notes list
    val next_follow_up: Long? = null, // epoch millis
    val call_refs: String = "[]", // JSON string of call IDs
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
