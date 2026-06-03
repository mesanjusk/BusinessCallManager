package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.leads.screen.LeadListScreen
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.LeadListVm

object LeadListRoute : NavRoute<LeadListVm> {
    override val route = Screen.LeadListScreen.route

    @Composable
    override fun viewModel(): LeadListVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: LeadListVm) = LeadListScreen(viewModel)
}
