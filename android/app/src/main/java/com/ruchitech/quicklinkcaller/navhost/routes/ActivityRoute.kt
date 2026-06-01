package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.activity.ActivityScreen
import com.ruchitech.quicklinkcaller.ui.screens.activity.ActivityViewModel

object ActivityRoute : NavRoute<ActivityViewModel> {
    override val route = Screen.ActivityScreen.route

    @Composable
    override fun viewModel(): ActivityViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: ActivityViewModel) = ActivityScreen(viewModel = viewModel)
}
