package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.*
import com.ruchitech.quicklinkcaller.room.data.Lead
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<Lead>)

    @Query("SELECT * FROM leads WHERE user_uuid = :userUuid ORDER BY created_at DESC")
    fun getAllLeads(userUuid: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE user_uuid = :userUuid AND status = :status ORDER BY created_at DESC")
    fun getLeadsByStatus(userUuid: String, status: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE lead_uuid = :leadUuid LIMIT 1")
    suspend fun getLeadById(leadUuid: String): Lead?

    @Query("SELECT * FROM leads WHERE phone = :phone LIMIT 1")
    suspend fun getLeadByPhone(phone: String): Lead?

    @Query("SELECT * FROM leads WHERE source = :source AND created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    suspend fun getLeadsBySourceBetween(source: String, start: Long, end: Long): List<Lead>

    @Query("SELECT * FROM leads WHERE isSynced = 0 AND user_uuid = :userUuid")
    suspend fun getUnsyncedLeads(userUuid: String): List<Lead>

    @Query("UPDATE leads SET isSynced = :synced WHERE lead_uuid = :leadUuid")
    suspend fun updateSyncStatus(leadUuid: String, synced: Boolean)

    @Query("UPDATE leads SET status = :status, updated_at = :updatedAt WHERE lead_uuid = :leadUuid")
    suspend fun updateLeadStatus(leadUuid: String, status: String, updatedAt: Long)

    @Delete
    suspend fun deleteLead(lead: Lead)

    @Query("SELECT COUNT(*) FROM leads WHERE user_uuid = :userUuid")
    suspend fun getLeadCount(userUuid: String): Int

    @Update
    suspend fun updateLead(lead: Lead)
}
