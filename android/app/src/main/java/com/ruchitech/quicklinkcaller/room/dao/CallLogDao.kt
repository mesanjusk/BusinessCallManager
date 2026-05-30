package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.*
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.ChildCallLogVm
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallLogs(callLogs: List<CallLogs>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallLog(callLogs: CallLogs): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCallLogDetail(callLogDetails: CallLogDetails): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLogDetails(callLogDetails: List<CallLogDetails>)

    @Query("SELECT COUNT(*) FROM Call_logs WHERE callerId = :callerId")
    suspend fun doesCallerIdExist(callerId: String): Int

    @Query("SELECT COUNT(*) FROM Call_log_details WHERE id = :callLogId")
    suspend fun doesCallLogExist(callLogId: Long): Int

    @Transaction
    @Query("SELECT * FROM Call_logs ORDER BY (SELECT MAX(date) FROM Call_log_details WHERE callerId = Call_logs.callerId) DESC")
    fun getAllCallLogsWithDetails(): Flow<List<CallLogsWithDetails>>

    @Query("SELECT * FROM Call_logs ORDER BY (SELECT MAX(date) FROM Call_log_details WHERE callerId = Call_logs.callerId) DESC LIMIT :pageSize OFFSET :offset")
    fun getPaginatedCallLogs(pageSize: Int, offset: Int): Flow<List<CallLogsWithDetails>>

    @Query("SELECT * FROM Call_logs ORDER BY (SELECT MAX(date) FROM Call_log_details WHERE callerId = Call_logs.callerId) DESC LIMIT :pageSize OFFSET :offset")
    suspend fun getPaginatedCallLogsTest(pageSize: Int, offset: Int): MutableList<CallLogsWithDetails>?


    // Query to get a CallLogs object based on callerId
    @Query("SELECT * FROM Call_log_details WHERE id = :byId")
    suspend fun getCallLogById(byId: Long): CallLogDetails?

    @Query("SELECT * FROM Call_log_details WHERE callerId = :callerId ORDER BY date DESC LIMIT 1")
    suspend fun getLastEntryByCallerId(callerId: String): CallLogDetails?

    @Query("SELECT * FROM Call_log_details  ORDER BY date DESC LIMIT 1")
    suspend fun getAllCallLogs(): List<CallLogDetails>

    @Query("SELECT * FROM Call_log_details ORDER BY date DESC")
    suspend fun getAllCallLogsForAnalytics(): List<CallLogDetails>

    @Query("SELECT * FROM Call_log_details WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    suspend fun getCallLogsBetween(start: Long, end: Long): List<CallLogDetails>

    @Query("SELECT * FROM Call_log_details WHERE date < :before ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentCallLogsBefore(before: Long, limit: Int): List<CallLogDetails>

    @Query("SELECT * FROM Call_logs WHERE callerId = :callerId")
    suspend fun getCallLogById(callerId: String): CallLogs?

    // Insert or update a CallLogs entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallLogs(callLogs: CallLogs)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallLogs(callLogs: CallLogDetails)

    // Insert or update multiple CallLogDetails entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCallLogDetails(callLogDetails: List<CallLogDetails>)

    @Query("SELECT * FROM Call_logs " +
            "WHERE callerId IN (SELECT DISTINCT callerId FROM Call_log_details " +
            "WHERE callNote LIKE '%' || :searchQuery || '%' OR number LIKE '%' || :searchQuery || '%' OR cachedName LIKE '%' || :searchQuery || '%' " +
            "UNION " +
            "SELECT DISTINCT contact_mobile as callerId FROM contacts WHERE contact_title LIKE '%' || :searchQuery || '%') " +
            "ORDER BY (SELECT MAX(date) FROM Call_log_details WHERE callerId = Call_logs.callerId) DESC")
    suspend fun searchCallLogs(searchQuery: String): List<CallLogsWithDetails>

    @Query("UPDATE Call_log_details SET cachedName = :newCachedName WHERE callerId = :callerId")
    suspend fun updateCachedNameByCallerId(callerId: String, newCachedName: String)


    //@Query("SELECT * FROM Call_log_details WHERE callerId LIKE '%' || :callerId || '%' ORDER BY date DESC LIMIT :pageSize OFFSET :offset")
    @Query("SELECT * FROM Call_log_details WHERE callerId = :callerId  ORDER BY date DESC LIMIT :pageSize OFFSET :offset")
    fun getPaginatedCallLogs(
        callerId: String,
        pageSize: Int,
        offset: Int
    ): Flow<List<CallLogDetails>?>


    @Query("SELECT * FROM Call_log_details WHERE isSynced = 0 AND date >= :startDate ORDER BY date DESC LIMIT :batchSize OFFSET :offset")
    suspend fun getUnsyncedCallLogs(offset: Int, batchSize: Int, startDate: Long): List<CallLogDetails>


    @Query("UPDATE call_log_details SET isSynced = :isSynced WHERE id IN (:contactIds)")
    suspend fun updateCallLogSynced(contactIds: List<Long>, isSynced: Boolean)

    @Query("SELECT * FROM Call_log_details ORDER BY id DESC LIMIT 1")
    suspend fun getLastCallLogDetails(): CallLogDetails?
    @Query("SELECT * FROM Call_log_details WHERE id = :id LIMIT 1")
    suspend fun logExits(id: Long): CallLogDetails?




    @Query("""
    SELECT 
        type, 
        COUNT(*) as typeCount, 
        SUM(duration) as totalDuration
    FROM Call_log_details
    WHERE  callerId =:callerId 
    GROUP BY type
    ORDER BY date DESC
""")
    suspend fun getCallTypeCountAndDurationByCallerId(
        callerId: String
    ): List<ChildCallLogVm.CallTypeCountDuration>




    //7489894441

}
