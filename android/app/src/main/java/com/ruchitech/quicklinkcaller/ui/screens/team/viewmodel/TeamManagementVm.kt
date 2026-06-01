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

    private val _joinError = MutableStateFlow<String?>(null)
    val joinError: StateFlow<String?> = _joinError.asStateFlow()

    private val _joinSuccess = MutableStateFlow(false)
    val joinSuccess: StateFlow<Boolean> = _joinSuccess.asStateFlow()

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

    fun joinBusiness(inviteCode: String) {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            // Add this user to the business with matching invite code
            // For now: store as a pending member locally, sync via backend when connected
            val existing = dbRepository.businessDao.getBusinessOnce()
            if (existing != null && existing.invite_code == inviteCode) {
                _joinError.value = "You are already in this business"
                return@launch
            }
            if (existing != null) {
                _joinError.value = "You are already part of another business"
                return@launch
            }
            // Create a placeholder business entry (will be overwritten on backend sync)
            dbRepository.businessDao.insertBusiness(Business(
                business_uuid = java.util.UUID.randomUUID().toString(),
                owner_uuid = userUuid,
                business_name = "Team Account",
                invite_code = inviteCode,
                team_members_json = "[{\"user_uuid\":\"$userUuid\",\"role\":\"member\"}]"
            ))
            _joinSuccess.value = true
            navigateUp()
        }
    }

    fun addMember(memberName: String, memberPhone: String) {
        viewModelScope.launch {
            val business = _business.value ?: return@launch
            val currentMembers = business.team_members_json
            val newMember = "{\"name\":\"$memberName\",\"phone\":\"$memberPhone\",\"role\":\"member\"}"
            val updatedJson = currentMembers.trimEnd(']') + (if (currentMembers == "[]") "" else ",") + "$newMember]"
            dbRepository.businessDao.updateBusiness(business.copy(team_members_json = updatedJson))
        }
    }
}
