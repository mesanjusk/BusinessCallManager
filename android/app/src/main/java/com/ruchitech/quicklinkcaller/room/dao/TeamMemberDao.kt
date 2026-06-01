package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.*
import com.ruchitech.quicklinkcaller.room.data.LeadAssignment
import com.ruchitech.quicklinkcaller.room.data.TeamMember

@Dao
interface TeamMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(teamMember: TeamMember): Long

    @Delete
    suspend fun delete(teamMember: TeamMember)

    @Query("SELECT * FROM team_members ORDER BY name ASC")
    suspend fun getAllTeamMembers(): List<TeamMember>

    @Query("SELECT * FROM team_members WHERE id = :id")
    suspend fun getById(id: Long): TeamMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignLead(assignment: LeadAssignment): Long

    @Query("SELECT * FROM lead_assignments WHERE teamMemberId = :teamMemberId ORDER BY assignedDate DESC")
    suspend fun getAssignmentsByTeamMember(teamMemberId: Long): List<LeadAssignment>

    @Query("SELECT * FROM lead_assignments WHERE callerId = :callerId LIMIT 1")
    suspend fun getAssignmentByCallerId(callerId: String): LeadAssignment?

    @Query("DELETE FROM lead_assignments WHERE callerId = :callerId")
    suspend fun deleteAssignmentByCallerId(callerId: String)

    @Query("SELECT COUNT(*) FROM lead_assignments WHERE teamMemberId = :teamMemberId")
    suspend fun getAssignmentCountByTeamMember(teamMemberId: Long): Int

    @Query("SELECT * FROM lead_assignments ORDER BY assignedDate DESC")
    suspend fun getAllAssignments(): List<LeadAssignment>
}
