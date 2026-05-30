package com.ruchitech.quicklinkcaller.workers

import android.content.Context
import android.util.Log
import androidx.room.util.copy
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Date

class UpdateCallLogsSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = MyApp.instance.dbRepository
        val batchSize = 200
        var offset = 0
        val currentDate = Date().time
        val getDays: Long = 31L * 24 * 60 * 60 * 1000
        val thirtyOneDaysAgo = Date(currentDate - getDays).time
        do {
            val batch = db.callLogDao.getUnsyncedCallLogs(offset, batchSize, thirtyOneDaysAgo)
            if (batch.isNotEmpty()) {
                try {
                    val passData = batch.filter { it -> !it.isSynced }
               syncBatchWithRetry(passData)
                } catch (e: Exception) {
                    // Log or handle the error
                    return Result.retry()
                }
            }

            offset += batchSize
        } while (batch.size == batchSize)

        return Result.success()
    }

    private suspend fun syncBatchWithRetry(batch: List<CallLogDetails>) {
        MyApp.instance.accountRepository.syncUpdateCallLogs(batch)
            .distinctUntilChanged()
            .collectLatest { resources ->
                when (resources.status) {
                    Status.INITIAL -> Unit
                    Status.EMPTY -> Unit
                    Status.SUCCESS -> {
                        Log.e("jhbgfrdghj", "success syncBatchWithRetry: ${resources.data}")
                        val data = resources.data
                        if (data?.isSuccess == true) {
                            MyApp.instance.dbRepository.callLogDao.updateCallLogSynced(
                                batch.map { it.id },
                                true
                            )
                        }
                    }

                    Status.ERROR -> {
                        Log.e("jhbgfrdghj", "syncBatchWithRetry: ${resources.data}")
                    }

                    Status.LOADING -> {

                    }
                }
            }
    }
}
