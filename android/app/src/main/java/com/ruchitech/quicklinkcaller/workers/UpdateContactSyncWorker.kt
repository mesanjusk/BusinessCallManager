package com.ruchitech.quicklinkcaller.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.room.data.Contact
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class UpdateContactSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = MyApp.instance.dbRepository
        val batchSize = 50 // Adjust the batch size as needed
        var offset = 0
        do {
            val batch = db.contact.getContactsInBatch(offset, batchSize)
            if (batch.isNotEmpty()) {
                try {
                    syncBatchWithRetry(batch)
                } catch (e: Exception) {
                    // Log or handle the error
                    return Result.retry()
                }
            }

            offset += batchSize
        } while (batch.size == batchSize)

        return Result.success()
    }

    private suspend fun syncBatchWithRetry(batch: List<Contact>) {
        MyApp.instance.accountRepository.syncUpdatedContacts(batch)
            .distinctUntilChanged()
            .collectLatest { resources ->
                when (resources.status) {
                    Status.INITIAL -> Unit
                    Status.EMPTY -> Unit
                    Status.SUCCESS -> {
                        val data = resources.data
                        if (data?.isSuccess == true) {
                            MyApp.instance.dbRepository.contact.updateIsSynced(data.result.map { it.contact_uuid }, true)
                        }
                    }

                    Status.ERROR -> {
                        Log.e("tag", "syncBatchWithRetry: ${resources.data}")
                    }

                    Status.LOADING -> {

                    }
                }
            }
    }
}
