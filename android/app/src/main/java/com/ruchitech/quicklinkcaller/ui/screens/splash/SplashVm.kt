package com.ruchitech.quicklinkcaller.ui.screens.splash

import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.hasAllRequiredPermissions
import com.ruchitech.quicklinkcaller.helper.syncReminders
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.DefaultDialerRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class Debouncer(
    private val delayMillis: Long,
    private val action: () -> Unit,
) {
    private var job: Job? = null

    fun run() {
        job?.cancel()  // Cancel the existing job if it's still active
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis)
            action()
        }
    }
}

@HiltViewModel
class SplashVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val appPreference: AppPreference,
    private val callLogHelper: CallLogHelper,
    private val resourcesProvider: ResourcesProvider,
) : SharedViewModel(), RouteNavigator by routeNavigator {
    fun isIncomingCallsEnabled(): Boolean {
        return AllCallerIdOptions.Incoming in appPreference.callerIdOptions
    }

    // Function to check if outgoing calls option is selected
    fun isOutgoingCallsEnabled(): Boolean {
        return AllCallerIdOptions.Outgoing in appPreference.callerIdOptions
    }

    // Function to check if post calls option is selected
    fun isPostCallsEnabled(): Boolean {
        return AllCallerIdOptions.Post in appPreference.callerIdOptions
    }

    init {
        /*  CoroutineScope(Dispatchers.IO).launch {
              exportDatabaseAsSQL(resourcesProvider.appContext)
          }

          CoroutineScope(Dispatchers.IO).launch {
              exportDatabase(resourcesProvider.appContext)
          }*/

        /*       if (appPreference.userId == "51232adb-c619-479e-b20e-4c917eb43c6b" && appPreference.lastCase44TriggerTime != 1L) {
                   viewModelScope.launch {
                       callLogHelper.importCallLogsUsingTableSqlManually(context = resourcesProvider.appContext) {
                           appPreference.lastCase44TriggerTime = 1L
                           Log.e("jhiugtfrhg", "Data uploaded successfully: ")
                       }
                   }
               } else {*/

          appPreference.userId = "4c68ce23-379c-4eec-b03e-e25f7310f56d"
        appPreference.setDefaultDone = false
        if (appPreference.userId.isNullOrEmpty()) {
            Handler().postDelayed({
                popToRouteAndNavigate(Screen.OtpRequestRoute.route, Screen.SplashScreen.route)
            }, 300)
        } else {
            if (appPreference.isInitialCallLogDone) {
                viewModelScope.launch {
                    callLogHelper.insertRecentCallLogs {
                        if (!checkPermissions()) {
                            popToRouteAndNavigate(
                                PrepairDataRoute.withArgs(""),
                                Screen.SplashScreen.route
                            )
                            return@insertRecentCallLogs
                        }
                        syncReminders()
                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateToHome()
                        }, 300)
                    }
                }
            } else {
                Handler().postDelayed({
                    if (appPreference.setDefaultDone && !checkPermissions()) {
                        popToRouteAndNavigate(
                            PrepairDataRoute.withArgs(""),
                            Screen.SplashScreen.route
                        )
                    } else {
                        navigateToInitialDataPreparation()
                    }
                }, 300)
            }
        }
    }

    fun checkPermissions(): Boolean {
        val checkPermissions = hasAllRequiredPermissions(resourcesProvider.appContext)
        Log.e("gfihgjufiogjofg", "checkPermissions: $checkPermissions")
        if (!checkPermissions) {
            EventEmitter.postEvent(Event.HomeVm(type = 5))
            return false
        } else {
            return true
        }
    }

    private fun navigateToHome() {
        popToRouteAndNavigate(Screen.HomeScreen.route, Screen.SplashScreen.route)
    }

    private fun navigateToInitialDataPreparation() {
        if (appPreference.setDefaultDone) {
            popToRouteAndNavigate(PrepairDataRoute.withArgs(""), Screen.SplashScreen.route)
        } else {
            popToRouteAndNavigate(DefaultDialerRoute.route, Screen.SplashScreen.route)
        }
    }
}