package com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.viewmodel

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.isTimeInFuture
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.recievers.AlarmReceiver
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.data.CallLogWithReminder
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NoteAndReminderVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val pref: AppPreference,
    private val resourcesProvider: com.ruchitech.quicklinkcaller.data.ResourcesProvider,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference,
    private val accountRepository: AccountRepository,
    private val callLogHelper: CallLogHelper
) : SharedViewModel(), RouteNavigator by routeNavigator {
    private val currentPage = mutableIntStateOf(0)
    private val pageSize = 8
    private var hasMoreData = true
    private var lastLoadTimestamp: Long = 0L
    private val minimumTimeDifference: Long =
        3000L  // Set your desired minimum time difference in milliseconds
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _callLogs = MutableStateFlow<List<CallLogWithReminder>>(emptyList())
    val callLogsData: StateFlow<List<CallLogWithReminder>> = _callLogs.asStateFlow()
    private var dateTimeString = ""
    var callLogForReminder by mutableLongStateOf(0L)
    var reminderSetObj by mutableStateOf<Reminders?>(null)
    private val _tasks = MutableStateFlow<List<Tasks?>?>(null)
    val tasks: StateFlow<List<Tasks?>?> = _tasks.asStateFlow()
    var orgTaskList = listOf<Tasks>()
    init {
        getCallNotes()
    }

    fun reInitNotes() {
        currentPage.intValue = 0
        _callLogs.value = emptyList()
        getCallNotes()
    }

    fun updateState(newData: List<CallLogWithReminder>) {
        _callLogs.value = newData
    }

    fun updateTasks(newData: MutableList<Tasks>, isInitial: Boolean = true) {
        _tasks.value = newData
    }

    fun loadMoreData() {
        // Increment the page number to load the next set of data
        if (hasMoreData) {
            val currentTimestamp = System.currentTimeMillis()
            // Check if enough time has passed since the last load
            if (currentTimestamp - lastLoadTimestamp >= minimumTimeDifference) {
                _isLoading.value = true
                currentPage.intValue += 1
                lastLoadTimestamp = currentTimestamp
                getCallNotes()
            }
        }
    }

    private fun getCallNotes() {
        viewModelScope.launch {
            val data = dbRepository.reminder.getCallLogsWithRemindersPaged(
                pageSize, currentPage.intValue * pageSize
            )
            if (data.isNotEmpty()) {
                if (data.size < pageSize) {
                    hasMoreData = false
                    if (callLogsData.value.size > 50) {
                        showSnackbar("No more data available")
                    }
                }
                if (currentPage.intValue != 0) {
                    delay(2000)
                }
                data.forEach { check ->
                    if (check.callLogDetails.cachedName.isNullOrEmpty() ||
                        check.callLogDetails.cachedName == "Unknown"
                    ) {
                        val checkName =
                            dbRepository.contact.getContactByPhoneNumber(
                                check.callLogDetails.number ?: ""
                            )?.contact_title

                        check.callLogDetails.colorCode = Orange
                        check.callLogDetails.cachedName = if (!checkName.isNullOrEmpty()) {
                            checkName
                        } else "Unknown"
                    }
                    val reminderData =
                        dbRepository.reminder.getCallerIdOptions(check.callLogDetails.id)
                    check.reminder = reminderData
                }

                val filteredData: ArrayList<CallLogWithReminder> = arrayListOf()

                data.forEach { item ->
                    if (item.reminder != null && item.callLogDetails.callNote.isNullOrEmpty()) {
                        if (isTimeInFuture(item.reminder?.timeInMillis ?: 0)) {
                            filteredData.add(item)
                        }
                    } else {
                        filteredData.add(item)
                    }
                }
                _callLogs.value = callLogsData.value.plus(filteredData)
                _isLoading.value = false
            }
        }
    }

    fun insertNoteOnCallLogChild(newNote: String, value: Long) {
        viewModelScope.launch {
            val dataToUpdate = dbRepository.callLogDao.getCallLogById(value)
            val tempData = dataToUpdate?.copy(callNote = newNote, isSynced = false)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
            }
            syncUpdateCallLogs()
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
            Log.e("kjhggfhj", "insertTasksOnChildLogs: $temp")
            val tempData =
                dataToUpdate?.copy(isSynced = false, tasks = temp)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
            }
            syncUpdateCallLogs()
        }
    }

    fun updateListFromDb(id:Long,onSuccess:(list:List<Tasks>)->Unit){
        viewModelScope.launch {
            val dataToUpdate = dbRepository.callLogDao.getCallLogById(id)
            Log.e("jkyhjij", "updateListFromDb: $dataToUpdate")
            orgTaskList = dataToUpdate?.tasks?: emptyList()
            onSuccess(dataToUpdate?.tasks?: emptyList())
        }
    }

    fun convertTimeStringToMillis(timeString: String): Long {
        val dateFormat = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(timeString)
        return date?.time ?: 0
    }

    private fun convertMillisTo24HourFormat(inMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = inMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)

        return String.format("%02d:%02d", hour, minute)
    }

    fun setAlarm(selectedDate: String, date: String, number: String, id: Long?): Reminders? {
        var data: Reminders? = null
        viewModelScope.launch {
            val inMillies = convertTimeStringToMillis(selectedDate)
            val getHourMin = convertMillisTo24HourFormat(inMillies)
            data = Reminders(
                id!!,
                getHourMin,
                date,
                callerId = number,
                0,
                true
            )
            dateTimeString = "$getHourMin:00 $date"
            setExactReminder(data!!)
        }
        Toast.makeText(
            resourcesProvider.appContext,
            "Reminder set successfully",
            Toast.LENGTH_SHORT
        ).show()
        return data
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
        val callerId = callLogForReminder
        resourcesProvider.appContext.cancelExistingReminder(callerId.toString())
        val alarmManager =
            resourcesProvider.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTimeString)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        data.timeInMillis = calendar.timeInMillis
        reminderSetObj = data
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
            callerId.toInt(),  // Use callerId as the requestCode to identify the PendingIntent
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

    fun openWhatsAppByNum(number: String) {
        resourcesProvider.appContext.openWhatsapp(number)
    }

    fun makeCallToNum(number: String) {
        resourcesProvider.appContext.makePhoneCall(number)
    }

}