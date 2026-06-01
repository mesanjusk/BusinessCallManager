package com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.LeadDetailRoute
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeadListVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _leads = MutableStateFlow<List<Lead>>(emptyList())
    val leads: StateFlow<List<Lead>> = _leads.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    init { loadLeads() }

    private fun loadLeads() {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            dbRepository.leadDao.getAllLeads(userUuid).collect { _leads.value = it }
        }
    }

    fun filterByStatus(status: String?) {
        _selectedStatus.value = status
        if (status == null) loadLeads()
        else viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            dbRepository.leadDao.getLeadsByStatus(userUuid, status).collect { _leads.value = it }
        }
    }

    fun openLead(leadUuid: String) = navigateToRoute(LeadDetailRoute.withArgs(leadUuid))

    fun addLead(phone: String, name: String?, source: String = "manual") {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            val lead = Lead(
                lead_uuid = java.util.UUID.randomUUID().toString(),
                user_uuid = userUuid,
                phone = phone,
                name = name,
                source = source
            )
            dbRepository.leadDao.insertLead(lead)
        }
    }
}
