package com.ruchitech.quicklinkcaller.navhost.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.routes.ActivityRoute
import com.ruchitech.quicklinkcaller.navhost.routes.AnalyticsRoute
import com.ruchitech.quicklinkcaller.navhost.routes.BusinessSetupRoute
import com.ruchitech.quicklinkcaller.navhost.routes.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.navhost.routes.DefaultDialerRoute
import com.ruchitech.quicklinkcaller.navhost.routes.HomeRoute
import com.ruchitech.quicklinkcaller.navhost.routes.JoinBusinessRoute
import com.ruchitech.quicklinkcaller.navhost.routes.LeadDetailRoute
import com.ruchitech.quicklinkcaller.navhost.routes.LeadListRoute
import com.ruchitech.quicklinkcaller.navhost.routes.NoteAndReminderRoute
import com.ruchitech.quicklinkcaller.navhost.routes.OtpRequestRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PipelineRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.navhost.routes.SettingsRoute
import com.ruchitech.quicklinkcaller.navhost.routes.SplashRoute
import com.ruchitech.quicklinkcaller.navhost.routes.TasksRoute
import com.ruchitech.quicklinkcaller.navhost.routes.TeamManagementRoute
import com.ruchitech.quicklinkcaller.navhost.routes.UpgradeRoute
import com.ruchitech.quicklinkcaller.navhost.routes.VerifyOtpRoute



@Composable
fun NavigationComponent(
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues
) {

    NavHost(
        route = "root",
        navController = navHostController,
        startDestination = Screen.SplashScreen.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        SplashRoute.composable(this, navHostController, snackbarHostState)
        OtpRequestRoute.composable(this, navHostController, snackbarHostState)
        PrepairDataRoute.composable(this, navHostController, snackbarHostState)
        HomeRoute.composable(this, navHostController, snackbarHostState)
        SettingsRoute.composable(this, navHostController, snackbarHostState)
        ChildCallLogRoute.composable(this, navHostController, snackbarHostState)
        VerifyOtpRoute.composable(this, navHostController, snackbarHostState)
        NoteAndReminderRoute.composable(this, navHostController, snackbarHostState)
        DefaultDialerRoute.composable(this, navHostController, snackbarHostState)
        AnalyticsRoute.composable(this, navHostController, snackbarHostState)
        LeadListRoute.composable(this, navHostController, snackbarHostState)
        PipelineRoute.composable(this, navHostController, snackbarHostState)
        LeadDetailRoute.composable(this, navHostController, snackbarHostState)
        TasksRoute.composable(this, navHostController, snackbarHostState)
        TeamManagementRoute.composable(this, navHostController, snackbarHostState)
        ActivityRoute.composable(this, navHostController, snackbarHostState)
        BusinessSetupRoute.composable(this, navHostController, snackbarHostState)
        JoinBusinessRoute.composable(this, navHostController, snackbarHostState)
        UpgradeRoute.composable(this, navHostController, snackbarHostState)
    }
}
