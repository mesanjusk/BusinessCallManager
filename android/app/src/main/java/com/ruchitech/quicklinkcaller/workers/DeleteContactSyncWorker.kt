package com.ruchitech.quicklinkcaller.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContact
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

class DeleteContactSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = MyApp.instance.dbRepository
        val batchSize = 50 // Adjust the batch size as needed
        var offset = 0
        do {
            val batch = db.contact.getAllDeletedContacts(offset, batchSize)
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

    private suspend fun syncBatchWithRetry(batch: List<DeleteSecondaryContact>) {
        MyApp.instance.accountRepository.syncDeletedContacts(batch)
            .distinctUntilChanged()
            .collectLatest { resources ->
                when (resources.status) {
                    Status.INITIAL -> Unit
                    Status.EMPTY -> Unit
                    Status.SUCCESS -> {
                        MyApp.instance.dbRepository.contact.deleteDeletedContactsByIds(batch.map { it.contact_uuid })
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
