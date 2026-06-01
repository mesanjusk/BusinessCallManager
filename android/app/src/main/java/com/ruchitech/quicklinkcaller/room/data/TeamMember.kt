package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val role: String = "Member",
    val created_at: Long = System.currentTimeMillis()
)
