package com.ruchitech.quicklinkcaller.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp

class TaskSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = MyApp.instance.dbRepository
            val unsynced = db.taskDao.getUnsyncedTasks()
            if (unsynced.isEmpty()) return Result.success()
            // TODO: call API to sync tasks
            unsynced.forEach { db.taskDao.updateSyncStatus(it.task_uuid, true) }
            Result.success()
        } catch (e: Exception) {
            Log.e("TaskSyncWorker", "sync failed: ${e.message}")
            Result.retry()
        }
    }
}
