package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.team.TeamScreen
import com.ruchitech.quicklinkcaller.ui.screens.team.TeamViewModel

object TeamRoute : NavRoute<TeamViewModel> {
    override val route: String = Screen.TeamScreen.route

    @Composable
    override fun viewModel(): TeamViewModel = hiltViewModel()

    @Composable
    override fun Content(viewModel: TeamViewModel) = TeamScreen(viewModel)
}
