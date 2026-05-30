package com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.isPhoneNumberValid
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.VerifyOtpRoute
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.SendOtp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpRequestVM @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val savedStateHandle: SavedStateHandle,
    private val preference: AppPreference,
    private val accountRepository: AccountRepository
) : SharedViewModel(), RouteNavigator by routeNavigator {

    var mobileNumber = mutableStateOf("")

    private fun safeApiCall() {
        viewModelScope.launch {
            showLoading()
            val phone = "91${mobileNumber.value}"
            accountRepository.sendOtp(SendOtp(mobile = phone))
                .distinctUntilChanged()
                .collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            delay(2000)
                            hideLoading()
                            showSnackbar(resources.message ?: "Failed to send OTP")
                        }
                        Status.SUCCESS -> {
                            hideLoading()
                            showSnackbar("OTP sent via WhatsApp")
                            navigateToRoute(VerifyOtpRoute.withArgs(phone, "na", "na"))
                        }
                        else -> Unit
                    }
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
