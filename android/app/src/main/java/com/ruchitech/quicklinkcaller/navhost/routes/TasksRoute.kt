package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.tasks.screen.TasksScreen
import com.ruchitech.quicklinkcaller.ui.screens.tasks.viewmodel.TasksVm

object TasksRoute : NavRoute<TasksVm> {
    override val route = Screen.TasksScreen.route

    @Composable
    override fun viewModel(): TasksVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: TasksVm) = TasksScreen(viewModel)
}
