package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.admin.screen.AdminDashboardScreen
import com.ruchitech.quicklinkcaller.ui.screens.admin.viewmodel.AdminDashboardVm

object AdminDashboardRoute : NavRoute<AdminDashboardVm> {
    override val route = Screen.AdminDashboardScreen.route

    @Composable
    override fun viewModel(): AdminDashboardVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: AdminDashboardVm) = AdminDashboardScreen(viewModel)
}
