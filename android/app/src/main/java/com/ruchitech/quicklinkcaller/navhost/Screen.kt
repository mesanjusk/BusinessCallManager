package com.ruchitech.quicklinkcaller.navhost

import com.ruchitech.quicklinkcaller.helper.Constant
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.DefaultDialerRoute
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.HomeRoute
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.NoteAndReminderRoute
import com.ruchitech.quicklinkcaller.helper.Constant.RoutePaths.SplashRoute

sealed class Screen(val route: String) {
    data object HomeScreen : Screen(route = HomeRoute)
    data object ChildCallLogScreen : Screen(route = ChildCallLogRoute)
    data object NoteAndReminderScreen : Screen(route = NoteAndReminderRoute)
    data object SplashScreen : Screen(route = SplashRoute)
    data object DefaultDialerScreen : Screen(route = DefaultDialerRoute)
    data object PrepareRoute : Screen(route = Constant.RoutePaths.PrepareRoute)
    data object SettingsRoute : Screen(route = Constant.RoutePaths.SettingsRoute)
    data object AnalyticsRoute : Screen(route = Constant.RoutePaths.AnalyticsRoute)
    data object OtpRequestRoute : Screen(route = Constant.RoutePaths.OtpRequestRouteName)
    data object OtpVerifyRoute : Screen(route = Constant.RoutePaths.VerifyOtpUiName)
    data object LeadListScreen : Screen(route = Constant.RoutePaths.LeadListRoute)
    data object LeadDetailScreen : Screen(route = Constant.RoutePaths.LeadDetailRoute)
    data object PipelineScreen : Screen(route = Constant.RoutePaths.PipelineRoute)
    data object TeamManagementScreen : Screen(route = Constant.RoutePaths.TeamManagementRoute)
    data object BusinessSetupScreen : Screen(route = Constant.RoutePaths.BusinessSetupRoute)
    data object JoinBusinessScreen : Screen(route = Constant.RoutePaths.JoinBusinessRoute)
    data object TasksScreen : Screen(route = Constant.RoutePaths.TasksRoute)
    data object UpgradeScreen : Screen(route = Constant.RoutePaths.UpgradeRoute)
    data object ActivityScreen : Screen(route = Constant.RoutePaths.ActivityRoute)
    data object AdminDashboardScreen : Screen(route = Constant.RoutePaths.AdminDashboardRoute)
}
