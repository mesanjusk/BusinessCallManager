package com.ruchitech.quicklinkcaller.ui.screens.default_dialer

import android.app.Activity.RESULT_CANCELED
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.WebViewActivity
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Constant
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.DefaultDialerRoute
import com.ruchitech.quicklinkcaller.navhost.routes.OtpRequestRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultDialerViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val resourcesProvider: ResourcesProvider,
    val preference: AppPreference,
    private val callLogHelper: CallLogHelper,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _uiState = MutableStateFlow(DefaultDialerUiState())
    val uiState: StateFlow<DefaultDialerUiState> = _uiState.asStateFlow()

    // Check if app is default dialer
    fun checkDefaultDialerStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val telecomManager =
                resourcesProvider.appContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val isDefault =
                telecomManager.defaultDialerPackage == resourcesProvider.appContext.packageName
            _uiState.update { it.copy(isDefaultDialer = isDefault) }
        } else {
            // Pre-Marshmallow, assume not default as we can't check
            _uiState.update { it.copy(isDefaultDialer = false) }
        }
    }

    // Request to become default dialer
    fun requestDefaultDialer() {
        _uiState.update { it.copy(isLoading = true) }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager =
                    resourcesProvider.appContext.getSystemService(Context.ROLE_SERVICE) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)

                _uiState.update { it.copy(pendingIntent = intent) }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                    putExtra(
                        TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                        resourcesProvider.appContext.packageName
                    )
                }
                //resourcesProvider.appContext.startActivity(intent)
                _uiState.update { it.copy(pendingIntent = intent) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Default dialer setting requires Android 6.0+"
                    )
                }
            }
        } catch (e: ActivityNotFoundException) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Could not find dialer settings"
                )
            }
        }
    }

    // Handle activity result from the dialer request
    fun handleActivityResult(result: ActivityResult) {
        Log.e("fdklhnngufodgnfo", "handleActivityResult: $result")
        updateState { it.copy(isLoading = false) }
        checkDefaultDialerStatus() // Re-check status after returning
        if (result.resultCode==RESULT_CANCELED){
            showSnackbar("Permission Not Enabled Or Getting Blocked By The Device")
        }else{
            onSkipClicked()
        }
    }

    // To update state, use this:
    private fun updateState(update: (DefaultDialerUiState) -> DefaultDialerUiState) {
        _uiState.value = update(_uiState.value)
    }

    // Example usage for clearing error
    fun clearErrorMessage() {
        updateState { it.copy(errorMessage = null) }
    }

    fun onSkipClicked() {
        preference.setDefaultDone = true
        if (preference.isInitialCallLogDone) {
            viewModelScope.launch {
                callLogHelper.insertRecentCallLogs {
                    Handler(Looper.getMainLooper()).postDelayed({
                        hideLoading()
                        navigateToHome()
                    }, 1000)
                }
            }
        } else {
            Handler().postDelayed({
                hideLoading()
                navigateToInitialDataPreparation()
            }, 200)
        }
    }


    private fun navigateToHome() {
        popToRouteAndNavigate(
            Screen.HomeScreen.route, DefaultDialerRoute.route, inclusive = true
        )
    }

    private fun navigateToInitialDataPreparation() {
        popToRouteAndNavigate(
            PrepairDataRoute.withArgs(""), DefaultDialerRoute.route, inclusive = true
        )
    }

    fun onPrivacyPolicyClicked() {
        val intent = Intent(resourcesProvider.appContext, WebViewActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            putExtra("url", Constant.Urls.privacyPolicy)
            putExtra("type", "Privacy Policy")
        }
        resourcesProvider.appContext.startActivity(intent)
    }

    companion object {
        const val REQUEST_CODE_DEFAULT_DIALER = 1001
        const val PRIVACY_POLICY_URL = "https://yourdomain.com/privacy"
    }
}

data class DefaultDialerUiState(
    val isLoading: Boolean = false,
    val isDefaultDialer: Boolean = false,
    val errorMessage: String? = null,
    val pendingIntent: Intent? = null
)