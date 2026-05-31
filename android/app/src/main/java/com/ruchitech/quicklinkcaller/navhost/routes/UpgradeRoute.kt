package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.premium.screen.UpgradeScreen
import com.ruchitech.quicklinkcaller.ui.screens.premium.viewmodel.UpgradeVm

object UpgradeRoute : NavRoute<UpgradeVm> {
    override val route = Screen.UpgradeScreen.route

    @Composable
    override fun viewModel(): UpgradeVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: UpgradeVm) = UpgradeScreen(viewModel)
}
