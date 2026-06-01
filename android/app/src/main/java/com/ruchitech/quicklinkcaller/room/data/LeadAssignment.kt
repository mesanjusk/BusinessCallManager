package com.ruchitech.quicklinkcaller.room.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lead_assignments")
data class LeadAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerId: String,
    val teamMemberId: Long,
    val assignedDate: Long = System.currentTimeMillis(),
    val note: String = ""
)
