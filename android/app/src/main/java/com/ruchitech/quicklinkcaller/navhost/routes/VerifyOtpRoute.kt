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
import com.ruchitech.quicklinkcaller.ui.screens.otp.ui.OtpVerifyUi
import com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel.OtpVerifyVM


object VerifyOtpRoute : NavRoute<OtpVerifyVM> {

    const val KEY_MOBILE_NUMBER = "KEY_MOBILE_NUMBER"

    const val KEY_TXNID = "KEY_TXNID"
    const val OTP_REQUEST_FOR = "OTP_REQUEST_FOR"

    override val route =
        "${Screen.OtpVerifyRoute.route}/{$KEY_MOBILE_NUMBER}/{$KEY_TXNID}/{$OTP_REQUEST_FOR}"

    fun withArgs(mobileNumber: String, countryCode: String, otpRequestFor: String): String = route
        .replace("{$KEY_MOBILE_NUMBER}", mobileNumber)
        .replace("{$KEY_TXNID}", countryCode)
        .replace("{$OTP_REQUEST_FOR}", otpRequestFor)

    fun getArgs(savedStateHandle: SavedStateHandle, key: String) =
        savedStateHandle.getOrThrow<String>(key)

    override fun getArguments(): List<NamedNavArgument> = listOf(
        navArgument(KEY_MOBILE_NUMBER) { type = NavType.StringType },
        navArgument(KEY_TXNID) { type = NavType.StringType },
        navArgument(OTP_REQUEST_FOR)
        { type = NavType.StringType })


    @Composable
    override fun viewModel(): OtpVerifyVM = hiltViewModel()

    @Composable
    override fun Content(
        viewModel: OtpVerifyVM
    ) = OtpVerifyUi(viewModel)
}
