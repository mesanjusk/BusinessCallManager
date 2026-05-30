package com.ruchitech.quicklinkcaller.contactutills

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.provider.CallLog.Calls.INCOMING_TYPE
import android.provider.CallLog.Calls.MISSED_TYPE
import android.provider.CallLog.Calls.OUTGOING_TYPE
import android.util.Log
import com.ruchitech.quicklinkcaller.contactutills.parsers.PhoneNumberParser
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.hasAllRequiredPermissions
import com.ruchitech.quicklinkcaller.helper.syncCallLogs
import com.ruchitech.quicklinkcaller.helper.syncReminders
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.room.data.Timestamp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("Range")
class CallLogHelper @Inject constructor(
    private val context: Context,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference,
    private val resource: ResourcesProvider,
    private val phoneNumberParser: PhoneNumberParser,
) {

    private val isSynced = true //  this is for test if dont want to sync call logs to server
    fun checkPermissions(): Boolean {

        val checkPermissions = hasAllRequiredPermissions(resource.appContext)
        if (!checkPermissions) {
            EventEmitter.postEvent(Event.HomeVm(type = 5))
            return false
        } else {
            return true
        }
    }

    suspend fun insertRecentCallLogs(onSuccess: () -> Unit) {
        if (!checkPermissions()) {
            onSuccess()
            return
        }
        withContext(Dispatchers.IO) {
            syncReminders()
            val lastInsertedCallLog = dbRepository.callLogDao.getLastCallLogDetails()
            val contentResolver: ContentResolver = context.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )

            val sortOrder = "${CallLog.Calls.DATE} DESC"
            val cursor: Cursor? = contentResolver.query(
                callLogUri,
                projection,
                null,
                null,
                sortOrder
            )

            val callLogsMap = mutableMapOf<String, MutableList<CallLogDetails>>()


            cursor?.use {
                while (it.moveToNext()) {
                    val callLogId = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                    if (lastInsertedCallLog?.id == callLogId) {
                        break
                    }
                    // Parse the phone number and get the map
                    val parsedMap = phoneNumberParser.phoneNumberData(
                        it.getString(it.getColumnIndex(CallLog.Calls.NUMBER)).trim(' '),
                        "IN"
                    )


                    val countryCode =
                        parsedMap["countryCode"] as? String // Country code (e.g., "91")
                    var phoneNumber =
                        parsedMap["number"] as? String // National number (e.g., "1234567890")
                    val regionCode = parsedMap["regionCode"] as? String // Region code (e.g., "IN")
                    val type =
                        parsedMap["type"] as? String // Type of number (e.g., "MOBILE", "FIXED_LINE")
                    val isValid =
                        parsedMap["isValid"] as? Boolean ?: false // Whether the number is valid

                    if (phoneNumber == null) {
                        phoneNumber = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                    }
                    val callType = when (it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))) {
                        INCOMING_TYPE -> CallType.INCOMING
                        OUTGOING_TYPE -> CallType.OUTGOING
                        MISSED_TYPE -> CallType.MISSED
                        else -> CallType.INCOMING
                    }

                    val callLogDetail = CallLogDetails(
                        id = callLogId,
                        callerId = phoneNumber,
                        cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        number = phoneNumber!!,
                        type = callType,
                        date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION)),
                        user_uuid = appPreference.userId,
                        countryCode = countryCode,
                        regionCode = regionCode,
                        typeOfNumber = type,
                        isValid = isValid,
                        isSynced = isSynced
                    )

                    if (callLogsMap.containsKey(phoneNumber)) {
                        callLogsMap[phoneNumber]?.add(callLogDetail)
                    } else {
                        callLogsMap[phoneNumber] = mutableListOf(callLogDetail)
                    }
                }
            }

            cursor?.close()
            var tempListCallDetails = arrayListOf<CallLogDetails>()
            val callLogs = callLogsMap.map { (callerId, callLogDetailsList) ->
                CallLogs(callerId = callerId).apply {
                    tempListCallDetails = ArrayList(callLogDetailsList)
                    callLogDetails = arrayListOf() //ArrayList(callLogDetailsList)
                    dbRepository.callLogDao.insertCallLogDetails(tempListCallDetails)
                }
            }
            if (callLogs.isNotEmpty()) {
                dbRepository.timestampDao.insertOrUpdateTimestamp(Timestamp(lastCallLogsSync = System.currentTimeMillis()))
            }
            dbRepository.callLogDao.insertCallLogs(callLogs)
            //dbRepository.callLogDao.insertCallLogDetails(callLogs.flatMap { it.callLogDetails })

            delay(100)
            updateLast15Calls()
            syncCallLogs()
            onSuccess.invoke()
        }
    }


    private suspend fun updateLast15Calls() {
        var limit = 0
        withContext(Dispatchers.IO) {
            syncReminders()
            val contentResolver: ContentResolver = context.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )

            val sortOrder = "${CallLog.Calls.DATE} DESC"

            val cursor: Cursor? = contentResolver.query(
                callLogUri,
                projection,
                null,
                null,
                sortOrder
            )

            val callLogsMap = mutableMapOf<String, MutableList<CallLogDetails>>()


            cursor?.use {
                while (it.moveToNext()) {
                    limit += 1
                    val callLogId = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                    if (limit == 16) {
                        break
                    }
                    // Parse the phone number and get the map
                    val parsedMap = phoneNumberParser.phoneNumberData(
                        it.getString(it.getColumnIndex(CallLog.Calls.NUMBER)).replace("", ""),
                        "IN"
                    )

                    val countryCode =
                        parsedMap["countryCode"] as? String // Country code (e.g., "91")
                    var phoneNumber =
                        parsedMap["number"] as? String // National number (e.g., "1234567890")
                    val regionCode = parsedMap["regionCode"] as? String // Region code (e.g., "IN")
                    val type =
                        parsedMap["type"] as? String // Type of number (e.g., "MOBILE", "FIXED_LINE")
                    val isValid =
                        parsedMap["isValid"] as? Boolean ?: false // Whether the number is valid

                    val callType = when (it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))) {
                        INCOMING_TYPE -> CallType.INCOMING
                        OUTGOING_TYPE -> CallType.OUTGOING
                        MISSED_TYPE -> CallType.MISSED
                        else -> CallType.INCOMING
                    }

                    val callLogDetail = CallLogDetails(
                        id = callLogId,
                        callerId = phoneNumber,
                        cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        number = phoneNumber!!,
                        type = callType,
                        date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION)),
                        user_uuid = appPreference.userId,
                        countryCode = countryCode,
                        regionCode = regionCode,
                        typeOfNumber = type,
                        isValid = isValid,
                        isSynced = isSynced
                    )

                    val logExits = dbRepository.callLogDao.logExits(callLogId)
                    if (logExits == null) {
                        if (callLogsMap.containsKey(phoneNumber)) {
                            callLogsMap[phoneNumber]?.add(callLogDetail)
                        } else {
                            callLogsMap[phoneNumber] = mutableListOf(callLogDetail)
                        }
                    }
                }
            }

            cursor?.close()
            var tempListCallDetails = arrayListOf<CallLogDetails>()
            val callLogs = callLogsMap.map { (callerId, callLogDetailsList) ->
                CallLogs(callerId = callerId).apply {
                    //   callLogDetails = ArrayList(callLogDetailsList)  this was working old
                    callLogDetails = arrayListOf()
                    // tempListCallDetails.addAll(callLogDetailsList)
                    dbRepository.callLogDao.insertCallLogDetails(callLogDetailsList)
                }
            }
            if (callLogs.isNotEmpty()) {
                dbRepository.callLogDao.insertCallLogs(callLogs)
                Log.e("giofyhgofg", "${tempListCallDetails.size}")
                Log.e("giofyhgofg", "${tempListCallDetails}")
                // dbRepository.callLogDao.insertCallLogDetails(tempListCallDetails)
            }

        }
    }


    suspend fun initializeCallLogs(onSuccess: () -> Unit) {
        withContext(Dispatchers.IO) {
            val contentResolver: ContentResolver = context.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )

            val sortOrder = "${CallLog.Calls.DATE} DESC"
            val batchSize = 1000 // Process call logs in chunks

            val cursor: Cursor? = contentResolver.query(
                callLogUri,
                projection,
                null,
                null,
                sortOrder
            )

            mutableMapOf<String, MutableList<CallLogDetails>>()

            cursor?.use {
                var processedCount = 0
                val tempListCallLog = mutableListOf<CallLogs>()
                val tempListCallDetails = mutableListOf<CallLogDetails>()

                while (it.moveToNext()) {
                    val callLogId = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                    val rawNumber =
                        it.getString(it.getColumnIndex(CallLog.Calls.NUMBER)) ?: continue
                    val parsedMap = phoneNumberParser.phoneNumberData(rawNumber, "IN")

                    val phoneNumber = parsedMap["number"] as? String ?: continue
                    val callType = when (it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))) {
                        INCOMING_TYPE -> CallType.INCOMING
                        OUTGOING_TYPE -> CallType.OUTGOING
                        MISSED_TYPE -> CallType.MISSED
                        else -> CallType.INCOMING
                    }

                    val callLogDetail = CallLogDetails(
                        id = callLogId,
                        callerId = phoneNumber,
                        cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        number = phoneNumber,
                        type = callType,
                        date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION)),
                        user_uuid = appPreference.userId,
                        countryCode = parsedMap["countryCode"] as? String,
                        regionCode = parsedMap["regionCode"] as? String,
                        typeOfNumber = parsedMap["type"] as? String,
                        isValid = parsedMap["isValid"] as? Boolean ?: false,
                        isSynced = isSynced
                    )

                    val callLog = CallLogs(callerId = phoneNumber)

                    /*
                                        if (callLogsMap.containsKey(phoneNumber)) {
                                            callLogsMap[phoneNumber]?.add(callLogDetail)
                                        } else {
                                            callLogsMap[phoneNumber] = mutableListOf(callLogDetail)
                                        }*/

                    processedCount++
                    tempListCallLog.add(callLog)
                    tempListCallDetails.add(callLogDetail)

                    // Process in batches
                    if (processedCount % batchSize == 0) {
                        insertCallLogsToDb(tempListCallLog, tempListCallDetails)
                        //callLogsMap.clear()
                        tempListCallDetails.clear()
                        tempListCallLog.clear()
                    }
                }

                // Insert remaining data
                if (tempListCallDetails.isNotEmpty()) {
                    insertCallLogsToDb(tempListCallLog, tempListCallDetails)
                }
            }

            appPreference.isInitialCallLogDone = true
            syncCallLogs()
            onSuccess.invoke()
        }
    }

    private suspend fun insertCallLogsToDb(
        callLogsMap: MutableList<CallLogs>,
        callLogDetailsList: List<CallLogDetails>,
    ) {
        dbRepository.timestampDao.insertOrUpdateTimestamp(
            Timestamp(lastCallLogsSync = System.currentTimeMillis())
        )
        dbRepository.callLogDao.insertCallLogs(callLogsMap)
        dbRepository.callLogDao.insertCallLogDetails(callLogDetailsList)
    }

}


