package com.ruchitech.quicklinkcaller.navhost.routes

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.ui.screens.leads.screen.LeadDetailScreen
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.LeadDetailVm

object LeadDetailRoute : NavRoute<LeadDetailVm> {
    const val KEY_LEAD_UUID = "leadUuid"

    override val route = Screen.LeadDetailScreen.route

    fun withArgs(leadUuid: String): String = "lead_detail_screen/$leadUuid"

    override fun getArguments() = listOf(
        navArgument(KEY_LEAD_UUID) { type = NavType.StringType }
    )

    @Composable
    override fun viewModel(): LeadDetailVm = hiltViewModel()

    @Composable
    override fun Content(viewModel: LeadDetailVm) = LeadDetailScreen(viewModel)
}
