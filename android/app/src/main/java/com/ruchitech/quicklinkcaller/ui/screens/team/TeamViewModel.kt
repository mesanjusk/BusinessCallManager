package com.ruchitech.quicklinkcaller.ui.screens.team

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.LeadAssignment
import com.ruchitech.quicklinkcaller.room.data.TeamMember
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamMemberWithStats(
    val member: TeamMember,
    val assignedLeadsCount: Int,
    val assignments: List<LeadAssignment>
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _teamMembers = MutableStateFlow<List<TeamMemberWithStats>>(emptyList())
    val teamMembers: StateFlow<List<TeamMemberWithStats>> = _teamMembers.asStateFlow()

    private val _allAssignments = MutableStateFlow<List<LeadAssignment>>(emptyList())
    val allAssignments: StateFlow<List<LeadAssignment>> = _allAssignments.asStateFlow()

    init {
        loadTeamMembers()
    }

    fun loadTeamMembers() {
        viewModelScope.launch {
            val members = dbRepository.teamMember.getAllTeamMembers()
            val membersWithStats = members.map { member ->
                val assignments = dbRepository.teamMember.getAssignmentsByTeamMember(member.id)
                TeamMemberWithStats(
                    member = member,
                    assignedLeadsCount = assignments.size,
                    assignments = assignments
                )
            }
            _teamMembers.value = membersWithStats
            _allAssignments.value = dbRepository.teamMember.getAllAssignments()
        }
    }

    fun addTeamMember(name: String, phone: String, email: String, role: String) {
        if (name.isBlank() || phone.isBlank()) {
            showSnackbar("Name and phone are required")
            return
        }
        viewModelScope.launch {
            dbRepository.teamMember.insert(
                TeamMember(
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    role = role.trim().ifBlank { "Member" }
                )
            )
            loadTeamMembers()
            showSnackbar("Team member added")
        }
    }

    fun deleteTeamMember(member: TeamMember) {
        viewModelScope.launch {
            dbRepository.teamMember.delete(member)
            loadTeamMembers()
            showSnackbar("Team member removed")
        }
    }

    fun assignLead(callerId: String, teamMemberId: Long, note: String = "") {
        viewModelScope.launch {
            // Remove existing assignment for this callerId
            dbRepository.teamMember.deleteAssignmentByCallerId(callerId)
            dbRepository.teamMember.assignLead(
                LeadAssignment(
                    callerId = callerId,
                    teamMemberId = teamMemberId,
                    note = note
                )
            )
            loadTeamMembers()
            showSnackbar("Lead assigned successfully")
        }
    }

    fun getAssignmentForCaller(callerId: String, onResult: (LeadAssignment?) -> Unit) {
        viewModelScope.launch {
            val assignment = dbRepository.teamMember.getAssignmentByCallerId(callerId)
            onResult(assignment)
        }
    }
}
