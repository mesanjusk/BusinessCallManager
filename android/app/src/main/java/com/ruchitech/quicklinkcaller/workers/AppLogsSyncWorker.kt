package com.ruchitech.quicklinkcaller.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.io.File

class AppLogsSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val appPreference = AppPreference(applicationContext)
        val userId = appPreference.userId ?: return Result.success()

        val logFile = File(applicationContext.getExternalFilesDir(null), "app_log.txt")
        if (!logFile.exists() || logFile.length() == 0L) return Result.success()

        val logs = logFile.readText()

        try {
            MyApp.instance.accountRepository.syncAppLogs(userId, logs)
                .distinctUntilChanged()
                .collectLatest { resources ->
                    when (resources.status) {
                        Status.SUCCESS -> Log.d("AppLogsSyncWorker", "App logs synced")
                        Status.ERROR -> Log.e("AppLogsSyncWorker", "Sync error: ${resources.message}")
                        else -> Unit
                    }
                }
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}
