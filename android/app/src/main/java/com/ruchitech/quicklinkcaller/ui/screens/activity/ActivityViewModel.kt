package com.ruchitech.quicklinkcaller.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class ActivityDateFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    CUSTOM("Date")
}

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _allCallLogs = MutableStateFlow<List<CallLogDetails>>(emptyList())
    val allCallLogs: StateFlow<List<CallLogDetails>> = _allCallLogs.asStateFlow()

    private val _whatsappMessages = MutableStateFlow<List<Lead>>(emptyList())
    val whatsappMessages: StateFlow<List<Lead>> = _whatsappMessages.asStateFlow()

    private val _whatsappCalls = MutableStateFlow<List<Lead>>(emptyList())
    val whatsappCalls: StateFlow<List<Lead>> = _whatsappCalls.asStateFlow()

    var selectedFilter by mutableStateOf(ActivityDateFilter.ALL)
        private set
    var customDateStart by mutableStateOf<Long?>(null)
        private set
    var customDateEnd by mutableStateOf<Long?>(null)
        private set

    init {
        loadLogs()
    }

    fun applyFilter(filter: ActivityDateFilter) {
        selectedFilter = filter
        if (filter != ActivityDateFilter.CUSTOM) {
            loadLogs()
        }
    }

    fun applyCustomDate(start: Long, end: Long) {
        customDateStart = start
        customDateEnd = end
        selectedFilter = ActivityDateFilter.CUSTOM
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            val (start, end) = getDateRange()

            // Call logs — filtered by date column
            val logs = if (selectedFilter == ActivityDateFilter.ALL) {
                dbRepository.callLogDao.getAllCallLogsForAnalytics()
            } else {
                dbRepository.callLogDao.getCallLogsBetween(start, end)
            }
            _allCallLogs.value = logs.sortedByDescending { it.date }

            // WhatsApp messages and calls from leads table
            val waMessages = if (selectedFilter == ActivityDateFilter.ALL) {
                dbRepository.leadDao.getLeadsBySource("whatsapp")
            } else {
                dbRepository.leadDao.getLeadsBySourceBetween("whatsapp", start, end)
            }
            _whatsappMessages.value = waMessages.sortedByDescending { it.updated_at }

            val waCalls = if (selectedFilter == ActivityDateFilter.ALL) {
                dbRepository.leadDao.getLeadsBySource("whatsapp_call")
            } else {
                dbRepository.leadDao.getLeadsBySourceBetween("whatsapp_call", start, end)
            }
            _whatsappCalls.value = waCalls.sortedByDescending { it.updated_at }
        }
    }

    private fun getDateRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis
        return when (selectedFilter) {
            ActivityDateFilter.ALL -> Pair(0L, end)
            ActivityDateFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, end)
            }
            ActivityDateFilter.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                Pair(cal.timeInMillis, end)
            }
            ActivityDateFilter.MONTH -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                Pair(cal.timeInMillis, end)
            }
            ActivityDateFilter.CUSTOM -> {
                Pair(customDateStart ?: 0L, customDateEnd ?: end)
            }
        }
    }
}
