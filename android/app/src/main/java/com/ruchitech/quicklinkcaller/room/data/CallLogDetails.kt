package com.ruchitech.quicklinkcaller.room.data

import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.ruchitech.quicklinkcaller.room.Converters


import androidx.compose.runtime.Stable

@Stable
data class CallLogsWithDetails(
    @Embedded val callLogs: CallLogs,
    @Relation(
        parentColumn = "callerId",
        entityColumn = "callerId"
    )
    var callLogDetails: MutableList<CallLogDetails>
)

@Stable
@Entity(tableName = "Call_logs", indices = [Index(value = ["callerId"], unique = true)])
data class CallLogs(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var callerId: String,
    var callNote: String? = ""
) {
    @TypeConverters(Converters::class)
    var callLogDetails: MutableList<CallLogDetails> = mutableListOf()
}


@Stable
@Entity(tableName = "Call_log_details", indices = [Index("callerId"), Index("date")])
data class CallLogDetails(
    @PrimaryKey val id: Long,
    val callerId: String?,
    var cachedName: String?,
    val number: String,
    val type: com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType,
    val date: Long,
    val duration: Long,
    var callNote: String? = null,
    var user_uuid: String?,
    var isSynced: Boolean = false,
    var tasks: List<Tasks> = listOf(),
    val countryCode: String? = null,
    val regionCode: String? = null ,// New column added
    val typeOfNumber: String? = null ,// New column added
    val isValid: Boolean = false ,// New column added
) {
    @Ignore
    var colorCode: Color = Color(0xFF454545)
    //var colorCode: Color = Color(0xFF6C63FF)
}


@Stable
data class Tasks(
    val callLogId: Long,
    val taskId: String,
    var tasks: String?,
    val date: Long,
    var strikeThrough: Boolean = false,
    var isDelete: Boolean = false
)

