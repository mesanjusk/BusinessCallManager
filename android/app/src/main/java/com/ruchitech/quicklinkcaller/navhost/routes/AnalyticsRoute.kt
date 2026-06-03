package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallAnalyticsScreen
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.CallAnalyticsVm

object AnalyticsRoute : NavRoute<CallAnalyticsVm> {
    override val route: String = Screen.AnalyticsRoute.route

    @Composable
    override fun viewModel(): CallAnalyticsVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: CallAnalyticsVm) = CallAnalyticsScreen(viewModel)
}
