package com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.recievers.AlarmReceiver
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class ChildCallLogVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val appPreference: AppPreference,
    private val callLogHelper: CallLogHelper,
    private val dbRepository: DbRepository,
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider
) : SharedViewModel(), RouteNavigator by routeNavigator {
    private val argsData =
        ChildCallLogRoute.getArgs(savedStateHandle, ChildCallLogRoute.KEY_CALLER_ID)
    private val currentPage = mutableIntStateOf(0)
    private val pageSize = 200
    private var lastLoadTimestamp: Long = 0L
    private var hasMoreData = true
    private val minimumTimeDifference: Long =
        3000L  // Set your desired minimum time difference in milliseconds
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val isNoteFieldOpen = mutableStateOf(false)
    private val _callLogs = MutableStateFlow<List<CallLogDetails>>(emptyList())
    val callLogsData: StateFlow<List<CallLogDetails>> = _callLogs.asStateFlow()
    val showReminderUi = mutableStateOf(false)
    private val _callLogForReminder = MutableStateFlow<CallLogDetails?>(null)
    val callLogForReminder: StateFlow<CallLogDetails?> = _callLogForReminder.asStateFlow()

    private val _reminder = MutableStateFlow<Reminders?>(null)
    val reminder: StateFlow<Reminders?> = _reminder.asStateFlow()

    private val _name = MutableStateFlow<String?>("Unknown")
    val name: StateFlow<String?> = _name.asStateFlow()


    private val _tasks = MutableStateFlow<List<Tasks?>?>(null)
    val tasks: StateFlow<List<Tasks?>?> = _tasks.asStateFlow()

    private val _tasks2 = MutableStateFlow<List<String>?>(null)
    val tasks2: StateFlow<List<String>?> = _tasks2.asStateFlow()

    private val _callLogData = MutableStateFlow<List<CallTypeCountDuration>>(emptyList())
    val callLogData: StateFlow<List<CallTypeCountDuration>> = _callLogData

    private var dateTimeString = ""


    data class CallTypeCountDuration(
        val type: CallType,
        val typeCount: Int,
        val totalDuration: Long
    )


    init {
        getPaginatedCallLogs()
        viewModelScope.launch {
            val normalizer = argsData //normalizePhoneNumber(argsData)
            val data = dbRepository.callLogDao.getCallTypeCountAndDurationByCallerId(normalizer)
            _callLogData.value = data
            Log.e("kjhgjghj", "$data:  $argsData")
        }
    }

    fun updateTasks(newData: MutableList<Tasks?>?) {
        _tasks.value = newData
    }

    fun updateTasks2(newData: MutableList<String>?) {
        _tasks2.value = newData
    }

    fun loadMoreData() {
        // Increment the page number to load the next set of data
        if (hasMoreData) {
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastLoadTimestamp >= minimumTimeDifference) {
                _isLoading.value = true
                currentPage.intValue += 1
                lastLoadTimestamp = currentTimestamp
                getPaginatedCallLogs()
            }
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove non-numeric characters and leading '0' or country code
        var normalizedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (normalizedNumber.startsWith("+91")) {
            // Remove country code if it exists
            normalizedNumber = normalizedNumber.substring(3)
        } else if (normalizedNumber.startsWith("0")) {
            // Remove leading '0'
            normalizedNumber = normalizedNumber.substring(1)
        }
        // Remove any '-' or ' ' characters
        normalizedNumber = normalizedNumber.replace(Regex("[-\\s]"), "")
        return normalizedNumber
    }

    private fun getPaginatedCallLogs() {
        val normalizer = argsData // normalizePhoneNumber(argsData)
        Log.e("lkojihfg", "getPaginatedCallLogs: $normalizer $argsData")
        viewModelScope.launch {
            dbRepository.callLogDao.getPaginatedCallLogs(
                normalizer,
                pageSize, currentPage.value * pageSize
            )
                .distinctUntilChanged()
                .collectLatest { callLogs ->
                    if (isNoteFieldOpen.value) {
                    } else {
                        if (!callLogs.isNullOrEmpty()) {
                            Log.e("c", "getPaginatedCallLogs: ${callLogs.size}")
                            val checkName = callLogs.maxByOrNull { it.date }?.cachedName
                            if (checkName == "Unknown" || checkName.isNullOrEmpty()) {
                                var tempName =
                                    dbRepository.contact.getContactByPhoneNumber(argsData)?.contact_title
                                if (tempName.isNullOrEmpty()) {
                                    tempName = "Unknown"
                                }
                                _name.value = tempName
                            } else {
                                _name.value = checkName
                            }

                            if (callLogs.size < pageSize) {
                                hasMoreData = false
                                if (callLogsData.value.size > 100) {
                                    showSnackbar("No more call logs available")
                                }
                            }
                            if (currentPage.intValue != 0) {
                                delay(1000)
                            }
                            val uniqueNewData = callLogs.distinctBy { it.id }
                            if (uniqueNewData.isNotEmpty()) {
                                _callLogs.value = _callLogs.value.plus(uniqueNewData)
                            } else {
                                _callLogs.value = _callLogs.value.plus(callLogs)
                            }
                            _isLoading.value = false

                        }
                    }
                }
        }
    }

    fun updateLog(tempList: MutableList<CallLogDetails>) {
        _callLogs.value = tempList
    }

    fun refreshPreviousScreen(callLogsWithDetails: CallLogDetails) {
        EventEmitter.postEvent(Event.HomeVm(4, callLogsWithDetails))
    }

    fun insertNoteOnCallLog(newNote: String, value: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataToUpdate = dbRepository.callLogDao.getCallLogById(value)
            val tempData = dataToUpdate?.copy(callNote = newNote, isSynced = false)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                syncUpdateCallLogs()
                delay(1200)
                isNoteFieldOpen.value = false
            }
        }
    }

    fun insertTasksOnChildLogs(value: Long) {
        viewModelScope.launch {
            val temp = arrayListOf<Tasks>()
            tasks.value?.forEach {
                if (it != null && !it.tasks.isNullOrEmpty()) {
                    temp.add(it)
                }
            }
            val dataToUpdate = dbRepository.callLogDao.getCallLogById(value)
            val tempData =
                dataToUpdate?.copy(isSynced = false, tasks = temp)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
            }
            syncUpdateCallLogs()
        }
    }


    fun convertTimeStringToMillis(timeString: String): Long {
        val dateFormat = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(timeString)
        return date?.time ?: 0
    }

    fun convertMillisTo24HourFormat(inMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = inMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)

        return String.format("%02d:%02d", hour, minute)
    }

    fun onAddNewAlarm(callLog: CallLogDetails, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _callLogForReminder.value = callLog
            _reminder.value =
                dbRepository.reminder.getReminderByCallerId(callLog.id)
            onSuccess()
            /*
                        if (!callLog.callNote.isNullOrEmpty()) {

                           // showReminderUi.value = true
                        } else {
                            Toast.makeText(
                                resourcesProvider.appContext,
                                "Please add a note first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
            */

        }
    }

    fun setAlarm(selectedDate: String, date: String, number: String, id: Long?) {
        viewModelScope.launch {
            val inMillis = convertTimeStringToMillis(selectedDate)
            val getHourMin = convertMillisTo24HourFormat(inMillis)
            val data = Reminders(
                id!!,
                getHourMin,
                date,
                callerId = number,
                0,
                true
            )
            dateTimeString = "$getHourMin:00 $date"
            setExactReminder(data)
        }
        Toast.makeText(
            resourcesProvider.appContext,
            "Reminder set successfully",
            Toast.LENGTH_SHORT
        ).show()
    }


    private suspend fun setExactReminder(data: Reminders) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val exactAlarmPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
            //val useExactAlarmPermission = Manifest.permission.USE_EXACT_ALARM

            if (ContextCompat.checkSelfPermission(
                    resourcesProvider.appContext,
                    exactAlarmPermission
                ) != PackageManager.PERMISSION_GRANTED

            /* ContextCompat.checkSelfPermission(
                 this,
                 useExactAlarmPermission
             ) != PackageManager.PERMISSION_GRANTED*/
            ) {
                Log.e("dliksfsd", "setAlarm: permission not granted")
                scheduleReminder(data)
                /*                // Request the permissions
                                ActivityCompat.requestPermissions(
                                    applicationContext as Activity,
                                    arrayOf(exactAlarmPermission, useExactAlarmPermission),
                                    223
                                )*/

            } else {
                // Permissions already granted, proceed with scheduling the exact alarm
                scheduleReminder(data)
            }
        } else {
            // For versions below Android 12, no need to check runtime permissions
            scheduleReminder(data)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    suspend fun scheduleReminder(data: Reminders) {
        // Cancel existing reminders for the same callerId
        val callerId = callLogForReminder.value?.id;
        resourcesProvider.appContext.cancelExistingReminder(callerId.toString())
        val alarmManager =
            resourcesProvider.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTimeString)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        data.timeInMillis = calendar.timeInMillis
        dbRepository.reminder.insertOrUpdateCallerIdOptions(data)

        val intent =
            Intent(
                McsConstants.REMINDER,
                null,
                resourcesProvider.appContext,
                AlarmReceiver::class.java
            )
        intent.putExtra("alarmID", data.reminderId)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            resourcesProvider.appContext,
            (callerId
                ?: 1).toInt(),  // Use callerId as the requestCode to identify the PendingIntent
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val heartbeatMsFor = McsConstants.ONE_MINUTE
        val i5 = Build.VERSION.SDK_INT
        if (i5 >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                reminderPendingIntent!!
            )
        } else if (i5 < 19) {
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] =
                reminderPendingIntent!!
        } else {
            val i6 = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + i6 * 3,
                i6,
                reminderPendingIntent!!
            )
        }
    }


    fun openKeyboardWithoutFocus() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    fun hideKeyboard() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

}