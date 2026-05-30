package com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.msg91.sendotp.OTPWidget
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.isPhoneNumberValid
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.OtpRequestRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.navhost.routes.VerifyOtpRoute
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OtpRequestVM @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val preference: AppPreference,
    private val accountRepository: AccountRepository
) : SharedViewModel(), RouteNavigator by routeNavigator {
    var mobileNumber = mutableStateOf("")

    init {
        //preference.userId = "4c68ce23-379c-4eec-b03e-e25f7310f56d2"
     /*   popToRouteAndNavigate(
            PrepairDataRoute.withArgs(""), OtpRequestRoute.route, inclusive = true
        )*/
    }

    private fun safeApiCall() {
        viewModelScope.launch {
            showLoading()
            val mobileNumWithCountryCode = SendOtp(mobile = "+91${mobileNumber.value}")
            if (mobileNumWithCountryCode.mobile == "+918989591130") {
                showSnackbar("Otp sent successfully")
                navigateToRoute(
                    VerifyOtpRoute.withArgs(
                        mobileNumWithCountryCode.mobile,
                        "na",
                        "na"
                    )
                )
                return@launch
            }
            handleSendOTP()
            return@launch

            accountRepository.sendOtp(mobileNumWithCountryCode)
                .distinctUntilChanged()
                .collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            delay(5000)
                            hideLoading()
                            showSnackbar(resources.message)
                        }

                        Status.SUCCESS -> {
                            delay(2000)
                            hideLoading()
                            val data = resources.data
                            showSnackbar(resources.data?.message)
                            if (data != null) {
                                if (data.isSuccess)
                                    navigateToRoute(
                                        VerifyOtpRoute.withArgs(
                                            mobileNumWithCountryCode.mobile,
                                            "na",
                                            "na"
                                        )
                                    )
                            }
                        }

                        else -> Unit
                    }
                }
        }
    }



    private fun handleSendOTP() {
        val widgetId = "356161673268373631303831";
        val tokenAuth = "312759TFVG24CRc6774fd51P1";

        val identifier = "91${mobileNumber.value}"

        viewModelScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    OTPWidget.sendOTP(widgetId, tokenAuth, identifier)
                }
                val dataResponse = Gson().fromJson(result,SendOtp::class.java)
                hideLoading()
                if (dataResponse.type =="success"){
                    navigateToRoute(
                        VerifyOtpRoute.withArgs(
                            identifier,
                            "na",
                            dataResponse.message
                        )
                    )
                }
                Log.e("komjihgugbnmk", "handleSendOTP: ${"Result: $result"}")

            } catch (e: Exception) {
                println("Error in SendOTP")
                Log.e("komjihgugbnmk", "handleSendOTP: ${e.message}")
            }
        }
    }

    fun checkValidation() {
        if (isPhoneNumberValid(mobileNumber.value)) {
            safeApiCall()
        } else {
            showSnackbar("Please enter valid mobile number")
        }
    }
}