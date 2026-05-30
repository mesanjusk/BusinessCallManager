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
}
