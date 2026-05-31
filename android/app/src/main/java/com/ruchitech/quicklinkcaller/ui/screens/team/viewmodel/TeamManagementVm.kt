package com.ruchitech.quicklinkcaller.ui.screens.team.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Business
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamManagementVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    val appPreference: AppPreference
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _business = MutableStateFlow<Business?>(null)
    val business: StateFlow<Business?> = _business.asStateFlow()

    init {
        viewModelScope.launch {
            dbRepository.businessDao.getBusiness().collect { _business.value = it }
        }
    }

    fun createBusiness(name: String) {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            val inviteCode = (1..6).map { ('A'..'Z').random() }.joinToString("")
            dbRepository.businessDao.insertBusiness(Business(
                business_uuid = java.util.UUID.randomUUID().toString(),
                owner_uuid = userUuid,
                business_name = name,
                invite_code = inviteCode
            ))
        }
    }
}
