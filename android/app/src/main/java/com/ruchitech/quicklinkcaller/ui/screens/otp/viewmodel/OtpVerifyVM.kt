package com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.msg91.sendotp.OTPWidget
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.OtpRequestRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.navhost.routes.VerifyOtpRoute
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtp
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.VerifyOtp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.its.asn1.EndEntityType.app
import javax.inject.Inject

@HiltViewModel
class OtpVerifyVM @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val preference: AppPreference,
    private val accountRepository: AccountRepository,
    private val callLogHelper: CallLogHelper,
    private val dbRepository: DbRepository,
    private val resourcesProvider: ResourcesProvider,
    savedStateHandle: SavedStateHandle,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    val argsMobileNumber = VerifyOtpRoute.getArgs(
        savedStateHandle, VerifyOtpRoute.KEY_MOBILE_NUMBER
    )
    private val argsProductionData = VerifyOtpRoute.getArgs(
        savedStateHandle, VerifyOtpRoute.KEY_TXNID
    )
    private val otpRequestFor = VerifyOtpRoute.getArgs(
        savedStateHandle, VerifyOtpRoute.OTP_REQUEST_FOR
    )

    var mobileNumber = mutableStateOf("")
    val filledOtp = mutableStateOf("")

    private val _verifyOtp = MutableStateFlow(VerifyOtp(argsMobileNumber, ""))
    private val verifyOtp: StateFlow<VerifyOtp> = _verifyOtp.asStateFlow()

    fun validationCheck() {
        if (filledOtp.value.length < 4) {
            showSnackbar("Please Enter 4 Digit OTP")
            return
        } else {
            _verifyOtp.value = _verifyOtp.value.copy(otp = filledOtp.value)
            if (verifyOtp.value.mobile == "+918989591130" && verifyOtp.value.otp == "1234") {
                preference.mobileNumber = "+918989591130"
                // preference.mobileNumber = "+919131414139"
                //preference.userId = "4c68ce23-379c-4eec-b03e-e25f7310f56d" // mm
                preference.userId = "4c68ce23-379c-4eec-b03e-e25f7310f56d" // demo app
                safeApiSyncFetchContactCall()
                return
            }
            safeApiCall()
        }
    }

    private  fun verifyToken(token:String){
        viewModelScope.launch {

            _verifyOtp.value = verifyOtp.value.copy(token = token)
            accountRepository.verifyOtp(verifyOtp.value)
                .distinctUntilChanged().collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            delay(2000)
                            hideLoading()
                            showSnackbar(resources.message)
                        }

                        Status.SUCCESS -> {
                            val data = resources.data
                            preference.mobileNumber = verifyOtp.value.mobile
                            preference.userId = data?.userId
                            safeApiSyncFetchContactCall()
                        }

                        else -> Unit
                    }
                }
        }

    }

    private fun safeApiCall() {
        handleVerifyOtp()
        return
    }

    private fun safeApiSyncFetchContactCall() {
        viewModelScope.launch {
            accountRepository.syncFetchedContacts()
                .distinctUntilChanged().collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            delay(2000)
                            hideLoading()
                            showSnackbar(resources.message)
                        }

                        Status.SUCCESS -> {
                            val data = resources.data
                            Log.e("gkjfddfog", "safeApiSyncFetchContactCall: $data")
                            showSnackbar(data?.message)
                            viewModelScope.launch {
                                if (!data?.contacts.isNullOrEmpty()) {
                                    dbRepository.contact.insertAll(data!!.contacts)
                                }
                                navigateToNewScreen()
                            }
                        }

                        else -> Unit
                    }
                }
        }
    }


    private fun handleVerifyOtp() {
        val widgetId = "356161673268373631303831";
        val tokenAuth = "312759TFVG24CRc6774fd51P1";

        val identifier = "91${mobileNumber.value}"
        viewModelScope.launch {
            Log.e(
                "kmjnhbgfffgvhbjk",
                "handleVerifyOtp: $widgetId, $tokenAuth, $otpRequestFor, ${verifyOtp.value.otp}"
            )
            try {

                val result = withContext(Dispatchers.IO) {
                    OTPWidget.verifyOTP(widgetId, tokenAuth, otpRequestFor, verifyOtp.value.otp)
                }
                val dataResponse = Gson().fromJson(result, SendOtp::class.java)
                if (dataResponse.type=="success"){
                    verifyToken(dataResponse.message)
                }
                Log.e("fhudhfdifhb", "handleSendOTP: ${"Result: $result"}")
                println("Result: $result")

            } catch (e: Exception) {
                println("Error in VerifyOTP")
            }
        }
    }

    private fun navigateToNewScreen() {
        hideLoading()
        popToRouteAndNavigate(
            Screen.DefaultDialerScreen.route,
            Screen.SplashScreen.route
        )
    }

    private fun navigateToHome() {
        popToRouteAndNavigate(
            Screen.HomeScreen.route, OtpRequestRoute.route, inclusive = true
        )
    }

    private fun navigateToInitialDataPreparation() {
        popToRouteAndNavigate(
            PrepairDataRoute.withArgs(""), OtpRequestRoute.route, inclusive = true
        )
    }
}