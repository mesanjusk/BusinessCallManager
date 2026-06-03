package com.ruchitech.quicklinkcaller.ui.screens.admin.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Business
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    val appPreference: AppPreference,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    data class MemberStats(
        val name: String,
        val phone: String,
        val totalCalls: Int,
        val incomingCalls: Int,
        val outgoingCalls: Int,
        val missedCalls: Int,
        val totalLeads: Int,
        val wonLeads: Int,
    )

    data class AdminState(
        val business: Business? = null,
        val totalTeamCalls: Int = 0,
        val totalTeamLeads: Int = 0,
        val totalTeamWon: Int = 0,
        val memberStats: List<MemberStats> = emptyList(),
        val recentActivity: List<ActivityItem> = emptyList(),
        val loading: Boolean = true,
        val isOwner: Boolean = false,
    )

    data class ActivityItem(
        val memberName: String,
        val action: String,
        val timestamp: Long,
    )

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    private val namePattern = Regex("\"name\":\"([^\"]+)\"")
    private val phonePattern = Regex("\"phone\":\"([^\"]+)\"")

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch(Dispatchers.IO) {
            val business = dbRepository.businessDao.getBusinessOnce()
            if (business == null) {
                _state.value = AdminState(loading = false)
                return@launch
            }

            val userUuid = appPreference.userId ?: ""
            val isOwner = business.owner_uuid == userUuid

            val names = namePattern.findAll(business.team_members_json).map { it.groupValues[1] }.toList()
            val phones = phonePattern.findAll(business.team_members_json).map { it.groupValues[1] }.toList()

            val allCallLogs = dbRepository.callLogDao.getAllCallLogsForAnalytics()
            val allLeads = dbRepository.leadDao.getAllLeadsOnce()

            // Build per-member stats using phone number matching
            val memberStatsList = names.mapIndexed { i, name ->
                val phone = phones.getOrNull(i) ?: ""
                val memberCalls = if (phone.isNotBlank()) {
                    allCallLogs.filter { log ->
                        val num = log.callerId ?: log.number
                        num.endsWith(phone.takeLast(8)) || phone.endsWith(num.takeLast(8))
                    }
                } else emptyList()

                val memberLeads = if (phone.isNotBlank()) {
                    allLeads.filter { lead ->
                        lead.phone.endsWith(phone.takeLast(8)) || phone.endsWith(lead.phone.takeLast(8))
                    }
                } else emptyList()

                MemberStats(
                    name = name,
                    phone = phone,
                    totalCalls = memberCalls.size,
                    incomingCalls = memberCalls.count { it.type == CallType.INCOMING },
                    outgoingCalls = memberCalls.count { it.type == CallType.OUTGOING },
                    missedCalls = memberCalls.count { it.type == CallType.MISSED },
                    totalLeads = memberLeads.size,
                    wonLeads = memberLeads.count { it.status == "Won" },
                )
            }

            // My own stats
            val myLeads = allLeads.filter { it.user_uuid == userUuid }
            val myLeadsWon = myLeads.count { it.status == "Won" }

            // Recent activity feed: last 20 call logs across team
            val recentActivity = allCallLogs
                .sortedByDescending { it.date }
                .take(20)
                .mapNotNull { log ->
                    val num = log.callerId ?: log.number
                    val matchedMember = phones.indexOfFirst { phone ->
                        phone.isNotBlank() && (num.endsWith(phone.takeLast(8)) || phone.endsWith(num.takeLast(8)))
                    }
                    if (matchedMember < 0) null else {
                        val action = when (log.type) {
                            CallType.INCOMING -> "received a call"
                            CallType.OUTGOING -> "made a call"
                            CallType.MISSED -> "missed a call"
                            else -> "had a call"
                        }
                        ActivityItem(
                            memberName = names[matchedMember],
                            action = action,
                            timestamp = log.date,
                        )
                    }
                }

            _state.value = AdminState(
                business = business,
                totalTeamCalls = allCallLogs.size,
                totalTeamLeads = allLeads.size,
                totalTeamWon = allLeads.count { it.status == "Won" },
                memberStats = memberStatsList,
                recentActivity = recentActivity,
                loading = false,
                isOwner = isOwner,
            )
        }
    }
}
