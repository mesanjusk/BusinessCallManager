package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import androidx.compose.runtime.Composable
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.ui.screens.activity.ActivityScreen
import com.ruchitech.quicklinkcaller.ui.screens.activity.ActivityViewModel
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.ui.NoteAndReminderUi
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.viewmodel.NoteAndReminderVm

typealias ComposableFun = @Composable () -> Unit

sealed class TabItem(
    var icon: Int,
    var title: String,
    var screen: ComposableFun,
) {
    class CallLogTab(homeVm: HomeVm) :
        TabItem(R.drawable.call_log_black, "Call Logs", { CallLogScreen(viewModel = homeVm) })

    class ShowContactsTab(homeVm: HomeVm) :
        TabItem(R.drawable.call_log_black, "Contacts", { ShowContactsUi(viewModel = homeVm) })

    class NotesTab(noteAndReminderVm: NoteAndReminderVm) :
        TabItem(R.drawable.call_log_black, "Actions", { NoteAndReminderUi(viewModel = noteAndReminderVm) })

    class ActivityTab(activityVm: ActivityViewModel) :
        TabItem(R.drawable.call_log_black, "Activity", { ActivityScreen(viewModel = activityVm) })
}
