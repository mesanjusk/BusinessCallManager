package com.ruchitech.quicklinkcaller.room.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions

@Entity(tableName = "temp_data")
data class TempDataEntity(
    @PrimaryKey val id: Long = 1, // Since there is only one row, set a fixed ID
    @ColumnInfo(name = "temp_notes") val tempNotesMap: MutableMap<String?,String>,
    @ColumnInfo(name = "temp_Tasks") val tempTasksMap: MutableMap<String?, List<Tasks?>>,
)
