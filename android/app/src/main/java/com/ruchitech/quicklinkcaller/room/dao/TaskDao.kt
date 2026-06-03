package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.*
import com.ruchitech.quicklinkcaller.room.data.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Query("SELECT * FROM tasks WHERE assigned_to_uuid = :userUuid ORDER BY due_date ASC")
    fun getMyTasks(userUuid: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE assigned_to_uuid = :userUuid AND status = :status ORDER BY due_date ASC")
    fun getMyTasksByStatus(userUuid: String, status: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE business_uuid = :businessUuid ORDER BY created_at DESC")
    fun getTeamTasks(businessUuid: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE lead_uuid = :leadUuid ORDER BY created_at DESC")
    fun getTasksForLead(leadUuid: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE task_uuid = :taskUuid LIMIT 1")
    suspend fun getTaskById(taskUuid: String): Task?

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Query("UPDATE tasks SET isSynced = :synced WHERE task_uuid = :taskUuid")
    suspend fun updateSyncStatus(taskUuid: String, synced: Boolean)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
