package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business")
data class Business(
    @PrimaryKey val business_uuid: String,
    val owner_uuid: String,
    val business_name: String,
    val plan: String = "free", // "free" | "premium"
    val subscription_expiry: Long? = null,
    val team_members_json: String = "[]", // JSON string
    val invite_code: String,
    val created_at: Long = System.currentTimeMillis()
)
