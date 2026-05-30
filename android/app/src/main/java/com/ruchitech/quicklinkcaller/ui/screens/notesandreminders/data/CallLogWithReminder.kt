package com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.data

import androidx.room.Embedded
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Reminders

data class CallLogWithReminder(
    @Embedded val callLogDetails: CallLogDetails,
    @Embedded(prefix = "reminder_") var reminder: Reminders?
)
