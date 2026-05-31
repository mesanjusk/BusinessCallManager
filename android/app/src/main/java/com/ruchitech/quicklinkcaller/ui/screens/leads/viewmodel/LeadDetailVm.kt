package com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
class LeadDetailVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    savedStateHandle: SavedStateHandle
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val leadUuid: String = savedStateHandle.get<String>(LeadDetailRoute.KEY_LEAD_UUID) ?: ""
    private val _lead = MutableStateFlow<Lead?>(null)
    val lead: StateFlow<Lead?> = _lead.asStateFlow()

    init {
        if (leadUuid.isNotEmpty()) {
            viewModelScope.launch { _lead.value = dbRepository.leadDao.getLeadById(leadUuid) }
        }
    }

    fun updateStatus(newStatus: String) {
        viewModelScope.launch {
            dbRepository.leadDao.updateLeadStatus(leadUuid, newStatus, System.currentTimeMillis())
            _lead.value = dbRepository.leadDao.getLeadById(leadUuid)
        }
    }
}
