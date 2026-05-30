package com.ruchitech.quicklinkcaller.navhost.routes


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavRoute
import com.ruchitech.quicklinkcaller.navhost.nav.getOrThrow
import com.ruchitech.quicklinkcaller.ui.screens.otp.ui.OtpRequestUi
import com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel.OtpRequestVM


object OtpRequestRoute : NavRoute<OtpRequestVM> {
    const val OTP_REQUEST_FOR = "OTP_REQUEST_FOR"

    override val route = Screen.OtpRequestRoute.route

    @Composable
    override fun viewModel(): OtpRequestVM = hiltViewModel()

/*    fun withArgs(otpRequestFor: String): String = route
        .replace("{${OTP_REQUEST_FOR}}", otpRequestFor)

    fun getArgs(savedStateHandle: SavedStateHandle, key: String) =
        savedStateHandle.getOrThrow<String>(key)

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(OTP_REQUEST_FOR) { type = NavType.StringType })*/

    @Composable
    override fun Content(
        viewModel: OtpRequestVM
    ) = OtpRequestUi(viewModel)
}
