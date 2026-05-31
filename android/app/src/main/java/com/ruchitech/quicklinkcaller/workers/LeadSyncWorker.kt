package com.ruchitech.quicklinkcaller.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp

class LeadSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = MyApp.instance.dbRepository
            val prefs = MyApp.instance.appPreference
            val userUuid = prefs.userId ?: return Result.success()
            val unsynced = db.leadDao.getUnsyncedLeads(userUuid)
            if (unsynced.isEmpty()) return Result.success()
            // TODO: call API to sync leads when accountRepository is extended
            // For now mark all as synced after batch
            unsynced.forEach { db.leadDao.updateSyncStatus(it.lead_uuid, true) }
            Result.success()
        } catch (e: Exception) {
            Log.e("LeadSyncWorker", "sync failed: ${e.message}")
            Result.retry()
        }
    }
}
