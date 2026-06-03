package com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
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
class PipelineVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference
) : SharedViewModel(), RouteNavigator by routeNavigator {

    val stages = listOf("New", "Contacted", "Interested", "Negotiation", "Won", "Lost")
    private val _leadsByStage = MutableStateFlow<Map<String, List<Lead>>>(emptyMap())
    val leadsByStage: StateFlow<Map<String, List<Lead>>> = _leadsByStage.asStateFlow()

    init {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            dbRepository.leadDao.getAllLeads(userUuid).collect { all ->
                _leadsByStage.value = stages.associateWith { stage -> all.filter { it.status == stage } }
            }
        }
    }

    fun moveToStage(lead: Lead, newStatus: String) {
        viewModelScope.launch {
            dbRepository.leadDao.updateLeadStatus(lead.lead_uuid, newStatus, System.currentTimeMillis())
        }
    }
}
