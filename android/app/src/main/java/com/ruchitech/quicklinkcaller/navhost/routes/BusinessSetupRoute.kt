package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.team.screen.BusinessSetupScreen
import com.ruchitech.quicklinkcaller.ui.screens.team.viewmodel.TeamManagementVm

object BusinessSetupRoute : NavRoute<TeamManagementVm> {
    override val route = Screen.BusinessSetupScreen.route

    @Composable
    override fun viewModel(): TeamManagementVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: TeamManagementVm) = BusinessSetupScreen(viewModel)
}
