package com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.ai.GeminiService
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

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val text: String) : AiState()
    data class Error(val message: String) : AiState()
}

@HiltViewModel
class LeadDetailVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference,
    private val geminiService: GeminiService,
    savedStateHandle: SavedStateHandle,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val leadUuid: String =
        savedStateHandle.get<String>(LeadDetailRoute.KEY_LEAD_UUID) ?: ""

    private val _lead = MutableStateFlow<Lead?>(null)
    val lead: StateFlow<Lead?> = _lead.asStateFlow()

    private val _aiScoreState = MutableStateFlow<AiState>(AiState.Idle)
    val aiScoreState: StateFlow<AiState> = _aiScoreState.asStateFlow()

    private val _aiMessageState = MutableStateFlow<AiState>(AiState.Idle)
    val aiMessageState: StateFlow<AiState> = _aiMessageState.asStateFlow()

    val hasGeminiKey: Boolean get() = !appPreference.geminiApiKey.isNullOrBlank()

    init {
        if (leadUuid.isNotEmpty()) {
            viewModelScope.launch {
                _lead.value = dbRepository.leadDao.getLeadById(leadUuid)
            }
        }
    }

    fun updateStatus(newStatus: String) {
        viewModelScope.launch {
            dbRepository.leadDao.updateLeadStatus(leadUuid, newStatus, System.currentTimeMillis())
            _lead.value = dbRepository.leadDao.getLeadById(leadUuid)
        }
    }

    fun scoreLeadWithAi() {
        val lead = _lead.value ?: return
        val apiKey = appPreference.geminiApiKey ?: return

        viewModelScope.launch {
            val callCount = try {
                dbRepository.callLogDao.getCallLogById(lead.phone)?.let { 1 } ?: 0
            } catch (e: Exception) { 0 }
            _aiScoreState.value = AiState.Loading
            val result = geminiService.scoreLeadIntelligence(
                apiKey = apiKey,
                name = lead.name,
                phone = lead.phone,
                status = lead.status,
                source = lead.source,
                notes = lead.notes,
                callCount = callCount,
            )
            _aiScoreState.value = result.fold(
                onSuccess = { AiState.Success(it) },
                onFailure = { AiState.Error(it.message ?: "AI error") }
            )
        }
    }

    fun composeWhatsAppMessage(purpose: String) {
        val lead = _lead.value ?: return
        val apiKey = appPreference.geminiApiKey ?: return
        viewModelScope.launch {
            _aiMessageState.value = AiState.Loading
            val result = geminiService.composeWhatsAppMessage(
                apiKey = apiKey,
                name = lead.name,
                status = lead.status,
                purpose = purpose,
            )
            _aiMessageState.value = result.fold(
                onSuccess = { AiState.Success(it) },
                onFailure = { AiState.Error(it.message ?: "AI error") }
            )
        }
    }

    fun resetAiScore() { _aiScoreState.value = AiState.Idle }
    fun resetAiMessage() { _aiMessageState.value = AiState.Idle }
}
