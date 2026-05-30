package com.ruchitech.quicklinkcaller.ui.screens.preparingdata

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.checkPermissionsAll
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrepareDataVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val callLogHelper: CallLogHelper,
    private val resourcesProvider: ResourcesProvider,
     val appPreference: AppPreference,
    private val dbRepository: DbRepository,
    savedStateHandle: SavedStateHandle
) : SharedViewModel(), RouteNavigator by routeNavigator {
    private val comingFrom =
        PrepairDataRoute.getArgs(
            savedStateHandle,
            PrepairDataRoute.COMING_FROM
        )
    private val appPreferences = AppPreferences(resourcesProvider.appContext)
    val callLogPermission = mutableStateOf(false)
    val contactsPermission = mutableStateOf(false)
    val makeCallPermission = mutableStateOf(false)
    val sendSMSPermission = mutableStateOf(true)
    val postNotificationPermission = mutableStateOf(false)
    val callerIDPermission = mutableStateOf(false)
    val appOverOtherGranted = mutableStateOf(false)
    val batteryOptimization = mutableStateOf(false)
    val dataLoading = mutableStateOf(false)
    val dataFetched = mutableStateOf(false)
    val permissionType = mutableIntStateOf(1)
    fun getData() {
        viewModelScope.launch {
            dataLoading.value = true
            callLogHelper.initializeCallLogs {
                viewModelScope.launch {
                    delay(1000)
                    dataLoading.value = false
                    dataFetched.value = true
                    appPreference.isFirstOpen = false
                    dbRepository.callerIDOptions.insertOrUpdateCallerIdOptions(
                        CallerIdOptionsEntity(
                            callerIdOptions = setOf(
                                AllCallerIdOptions.Incoming,
                                AllCallerIdOptions.Outgoing,
                                AllCallerIdOptions.Post
                            ),
                            callerIdType = 1
                        )
                    )
                    appPreference.shouldForeground = true
                    delay(3000)
                    hideLoading()
                    popToRouteAndNavigate(
                        Screen.HomeScreen.route,
                        PrepairDataRoute.withArgs(comingFrom)
                    )
                }
            }
        }
    }

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        appOverOtherGranted.value = Settings.canDrawOverlays(resourcesProvider.appContext)
        batteryOptimization.value = isIgnoringBatteryOptimizations()
        checkPermissionsAll(resourcesProvider.appContext).forEach {
            when (it) {
                Manifest.permission.READ_CALL_LOG -> {
                    callLogPermission.value = true
                }

                Manifest.permission.READ_CONTACTS -> {
                    contactsPermission.value = true
                }

                Manifest.permission.CALL_PHONE -> {
                    makeCallPermission.value = true
                }

                Manifest.permission.SEND_SMS -> {
                    sendSMSPermission.value = true
                }

                Manifest.permission.POST_NOTIFICATIONS -> {
                    postNotificationPermission.value = true
                }

                Manifest.permission.SYSTEM_ALERT_WINDOW -> appOverOtherGranted.value = true
            }
        }
    }

    fun startDataProcessing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            postNotificationPermission.value = true
        }
        if (callLogPermission.value &&
            contactsPermission.value &&
            makeCallPermission.value &&
            sendSMSPermission.value &&
            appOverOtherGranted.value
        ) {
            showLoading()
            if (comingFrom == "settings") {
                navigateUp()
            } else {
                getData()
            }
        } else {
            showSnackbar("Please give all the permission before continue")
        }
    }

     fun isIgnoringBatteryOptimizations(): Boolean {
        return (resourcesProvider.appContext.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
            resourcesProvider.appContext.getPackageName()
        )
    }

    override fun handleInternalEvent(event: Event) {
        when (event) {
            is Event.HomeVm -> Unit
            is Event.PermissionHandler -> {
                checkPermissions()
            }
        }
    }
}