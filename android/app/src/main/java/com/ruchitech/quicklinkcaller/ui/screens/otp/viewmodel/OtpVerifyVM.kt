package com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.OtpRequestRoute
import com.ruchitech.quicklinkcaller.navhost.routes.VerifyOtpRoute
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.otp.data.VerifyOtp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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

    val argsMobileNumber = VerifyOtpRoute.getArgs(savedStateHandle, VerifyOtpRoute.KEY_MOBILE_NUMBER)

    var mobileNumber = mutableStateOf("")
    val filledOtp = mutableStateOf("")

    private val _verifyOtp = MutableStateFlow(VerifyOtp(argsMobileNumber, ""))
    private val verifyOtp: StateFlow<VerifyOtp> = _verifyOtp.asStateFlow()

    fun validationCheck() {
        if (filledOtp.value.length < 6) {
            showSnackbar("Please Enter 6 Digit OTP")
            return
        }
        _verifyOtp.value = _verifyOtp.value.copy(otp = filledOtp.value)
        safeApiCall()
    }

    private fun safeApiCall() {
        viewModelScope.launch {
            showLoading()
            accountRepository.verifyOtp(verifyOtp.value)
                .distinctUntilChanged()
                .collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            delay(2000)
                            hideLoading()
                            showSnackbar(resources.message ?: "Verification failed")
                        }
                        Status.SUCCESS -> {
                            val data = resources.data
                            preference.mobileNumber = "+" + verifyOtp.value.mobile.trimStart('+')
                            preference.userId = data?.userId
                            safeApiSyncFetchContactCall()
                        }
                        else -> Unit
                    }
                }
        }
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
                            Log.e("OtpVerifyVM", "syncContacts: $data")
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

    private fun navigateToNewScreen() {
        hideLoading()
        popToRouteAndNavigate(Screen.DefaultDialerScreen.route, Screen.SplashScreen.route)
    }

    private fun navigateToHome() {
        popToRouteAndNavigate(Screen.HomeScreen.route, OtpRequestRoute.route, inclusive = true)
    }
}
