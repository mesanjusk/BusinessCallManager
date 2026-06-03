package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.leads.screen.PipelineScreen
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.PipelineVm

object PipelineRoute : NavRoute<PipelineVm> {
    override val route = Screen.PipelineScreen.route

    @Composable
    override fun viewModel(): PipelineVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: PipelineVm) = PipelineScreen(viewModel)
}
