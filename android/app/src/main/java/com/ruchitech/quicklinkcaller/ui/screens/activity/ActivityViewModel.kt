package com.ruchitech.quicklinkcaller.ui.screens.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class ActivityDateFilter(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    CUSTOM("Date")
}

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _allCallLogs = MutableStateFlow<List<CallLogDetails>>(emptyList())
    val allCallLogs: StateFlow<List<CallLogDetails>> = _allCallLogs.asStateFlow()

    private val _whatsappLogs = MutableStateFlow<List<CallLogDetails>>(emptyList())
    val whatsappLogs: StateFlow<List<CallLogDetails>> = _whatsappLogs.asStateFlow()

    private val _whatsappCallLogs = MutableStateFlow<List<CallLogDetails>>(emptyList())
    val whatsappCallLogs: StateFlow<List<CallLogDetails>> = _whatsappCallLogs.asStateFlow()

    var selectedFilter by mutableStateOf(ActivityDateFilter.TODAY)
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
            val logs = dbRepository.callLogDao.getCallLogsBetween(start, end)
            val sorted = logs.sortedByDescending { it.date }
            _allCallLogs.value = sorted
            _whatsappLogs.value = sorted.distinctBy { it.callerId }
            _whatsappCallLogs.value = sorted.filter {
                it.type == com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.OUTGOING ||
                it.type == com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
            }
        }
    }

    private fun getDateRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis
        return when (selectedFilter) {
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
