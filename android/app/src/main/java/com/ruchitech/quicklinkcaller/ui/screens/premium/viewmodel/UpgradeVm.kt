package com.ruchitech.quicklinkcaller.ui.screens.premium.viewmodel

import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpgradeVm @Inject constructor(
    private val routeNavigator: RouteNavigator
) : SharedViewModel(), RouteNavigator by routeNavigator
