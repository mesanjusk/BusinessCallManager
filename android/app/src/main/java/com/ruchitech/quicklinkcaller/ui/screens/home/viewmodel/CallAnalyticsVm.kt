package com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CallAnalyticsVm @Inject constructor(
    private val dbRepository: DbRepository,
    private val routeNavigator: RouteNavigator,
    private val appPreference: AppPreference,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    data class TopNumber(
        val number: String,
        val name: String?,
        val totalCalls: Int,
        val totalDuration: Long,
        val lastDate: Long
    )

    data class LeadFunnelItem(val status: String, val count: Int, val color: Long)

    data class CallAnalyticsState(
        val totalCalls: Int = 0,
        val incoming: Int = 0,
        val outgoing: Int = 0,
        val missed: Int = 0,
        val totalDurationSec: Long = 0,
        val topByCount: TopNumber? = null,
        val topByDuration: TopNumber? = null,
        val hourlyBuckets: List<Int> = List(24) { 0 },
        val weekdayBuckets: List<Int> = List(7) { 0 },
        val answeredRate: Float = 0f,
        val avgDurationSec: Float = 0f,
        val deltaTotal: Int = 0,
        val deltaAnsweredRate: Float = 0f,
        val deltaAvgDurationSec: Float = 0f,
        val followUps: List<TopNumber> = emptyList(),
        val newContacts: Int = 0,
        val returningContacts: Int = 0,
        val shortCalls: Int = 0,
        val shortCallRate: Float = 0f,
        val longTalkers: List<LongTalker> = emptyList(),
        val busiestHour: Int? = null,
        val busiestDay: Int? = null, // 0=Sun
        val longestCall: LongCall? = null,
        val topNewContacts: List<TopNumber> = emptyList(),
        val loading: Boolean = true,
        val rangeLabel: String = "Last 30 days",
        // Lead funnel
        val totalLeads: Int = 0,
        val leadsWon: Int = 0,
        val leadsLost: Int = 0,
        val leadsInProgress: Int = 0,
        val newLeadsInRange: Int = 0,
        val leadConversionRate: Float = 0f,
        val leadFunnel: List<LeadFunnelItem> = emptyList(),
        val avgLeadAgeHours: Float = 0f,
    )

    private val _state = MutableStateFlow(CallAnalyticsState())
    val state: StateFlow<CallAnalyticsState> = _state.asStateFlow()

    data class LongTalker(
        val number: String,
        val name: String?,
        val avgDurationSec: Float,
        val calls: Int,
        val lastDate: Long
    )

    data class LongCall(
        val number: String,
        val name: String?,
        val durationSec: Long,
        val date: Long
    )

    sealed class Range(val label: String, val startMillis: Long?, val endMillis: Long?) {
        object Today : Range("Today", startOfDay(), System.currentTimeMillis())
        object SevenDays : Range("Last 7 days", System.currentTimeMillis() - 7 * ONE_DAY, System.currentTimeMillis())
        object ThirtyDays : Range("Last 30 days", System.currentTimeMillis() - 30 * ONE_DAY, System.currentTimeMillis())
        object AllTime : Range("All time", null, null)
        data class Custom(val start: Long, val end: Long) : Range("Custom", start, end)
    }

    init {
        refresh(Range.ThirtyDays)
    }

    fun refresh(range: Range) {
        _state.value = _state.value.copy(loading = true, rangeLabel = range.label)
        viewModelScope.launch(Dispatchers.IO) {
            val nowRange = resolveRange(range)
            val prevRange = previousRange(nowRange)

            val currentLogs = dbRepository.callLogDao.getCallLogsBetween(nowRange.first, nowRange.second)
            val prevLogs = dbRepository.callLogDao.getCallLogsBetween(prevRange.first, prevRange.second)
            val allLogs = dbRepository.callLogDao.getAllCallLogsForAnalytics()

            val filtered = currentLogs
            val total = filtered.size
            val incoming = filtered.count { it.type == CallType.INCOMING }
            val outgoing = filtered.count { it.type == CallType.OUTGOING }
            val missed = filtered.count { it.type == CallType.MISSED }
            val answeredCalls = filtered.count { it.type != CallType.MISSED && it.duration > 0 }
            val answeredRate = if (total == 0) 0f else answeredCalls.toFloat() / total.toFloat()
            val avgDuration = if (filtered.isEmpty()) 0f else filtered.sumOf { it.duration }.toFloat() / filtered.size.toFloat()
            val totalDuration = filtered.sumOf { it.duration }

            val grouped = filtered.groupBy { it.callerId ?: it.number }
            val topByCount = grouped.maxByOrNull { it.value.size }?.toTopNumber()
            val topByDuration = grouped.maxByOrNull { it.value.sumOf { it.duration } }?.toTopNumber(byDuration = true)

            val firstSeenMap = allLogs.groupBy { it.callerId ?: it.number }
                .mapValues { entry -> entry.value.minOfOrNull { it.date } ?: Long.MAX_VALUE }
            val activeCallers = grouped.keys.filterNotNull()
            val newContacts = activeCallers.count { caller ->
                val firstSeen = firstSeenMap[caller] ?: Long.MAX_VALUE
                firstSeen in nowRange.first..nowRange.second
            }
            val returningContacts = activeCallers.size - newContacts

            val shortCalls = filtered.count { it.duration in 1..10 }
            val shortRate = if (total == 0) 0f else shortCalls.toFloat() / total.toFloat()

            val longTalkers = grouped.entries
                .mapNotNull { (num, calls) ->
                    val avg = if (calls.isEmpty()) 0f else calls.sumOf { it.duration }.toFloat() / calls.size.toFloat()
                    if (avg < 180f) null else {
                        val name = calls.firstNotNullOfOrNull { it.cachedName.takeIf { n -> !n.isNullOrBlank() && n != "Unknown" } }
                        LongTalker(
                            number = num ?: "",
                            name = name,
                            avgDurationSec = avg,
                            calls = calls.size,
                            lastDate = calls.maxOfOrNull { it.date } ?: 0L
                        )
                    }
                }
                .sortedByDescending { it.avgDurationSec }
                .take(5)

            val hourly = MutableList(24) { 0 }
            val weekday = MutableList(7) { 0 } // 0 = Sunday
            filtered.forEach { log ->
                val cal = Calendar.getInstance().apply { timeInMillis = log.date }
                hourly[cal.get(Calendar.HOUR_OF_DAY)] = hourly[cal.get(Calendar.HOUR_OF_DAY)] + 1
                weekday[cal.get(Calendar.DAY_OF_WEEK) - 1] = weekday[cal.get(Calendar.DAY_OF_WEEK) - 1] + 1
            }
            val busiestHour = hourly.withIndex().maxByOrNull { it.value }?.takeIf { it.value > 0 }?.index
            val busiestDay = weekday.withIndex().maxByOrNull { it.value }?.takeIf { it.value > 0 }?.index

            // Comparison vs previous window
            val prevTotal = prevLogs.size
            val prevAnswered = prevLogs.count { it.type != CallType.MISSED && it.duration > 0 }
            val prevAnsweredRate = if (prevTotal == 0) 0f else prevAnswered.toFloat() / prevTotal.toFloat()
            val prevAvgDuration = if (prevLogs.isEmpty()) 0f else prevLogs.sumOf { it.duration }.toFloat() / prevLogs.size.toFloat()

            // Missed without callback (last 72h inside current range)
            val followUps = buildFollowUps(filtered, windowMillis = THREE_DAYS)

            val longestCall = filtered.maxByOrNull { it.duration }?.let {
                LongCall(
                    number = it.callerId ?: it.number,
                    name = it.cachedName?.takeIf { n -> !n.isNullOrBlank() && n != "Unknown" },
                    durationSec = it.duration,
                    date = it.date
                )
            }

            val topNewContacts = activeCallers
                .filter { caller ->
                    val firstSeen = firstSeenMap[caller] ?: Long.MAX_VALUE
                    firstSeen in nowRange.first..nowRange.second
                }.mapNotNull { caller ->
                    val entries = grouped[caller] ?: return@mapNotNull null
                    val lastDate = entries.maxOfOrNull { it.date } ?: 0L
                    TopNumber(
                        number = caller,
                        name = entries.firstNotNullOfOrNull { it.cachedName.takeIf { n -> !n.isNullOrBlank() && n != "Unknown" } },
                        totalCalls = entries.size,
                        totalDuration = entries.sumOf { it.duration },
                        lastDate = lastDate
                    )
                }.sortedByDescending { it.lastDate }.take(5)

            // Lead funnel
            val userUuid = appPreference.userId ?: ""
            val allLeads = dbRepository.leadDao.getAllLeadsOnce()
            val leadsInRange = allLeads.filter { it.created_at in nowRange.first..nowRange.second }
            val statusGroups = allLeads.groupBy { it.status }
            val wonCount = statusGroups["Won"]?.size ?: 0
            val lostCount = statusGroups["Lost"]?.size ?: 0
            val newCount = statusGroups["New"]?.size ?: 0
            val inProgressCount = allLeads.size - wonCount - lostCount - newCount
            val conversionRate = if (allLeads.isEmpty()) 0f else wonCount.toFloat() / allLeads.size.toFloat()
            val avgAgeHours = if (allLeads.isEmpty()) 0f else {
                val now = System.currentTimeMillis()
                allLeads.map { (now - it.created_at) / 3600000f }.average().toFloat()
            }
            val funnel = listOf(
                LeadFunnelItem("New", newCount, 0xFF1976D2),
                LeadFunnelItem("In Progress", inProgressCount.coerceAtLeast(0), 0xFFFF8F00),
                LeadFunnelItem("Won", wonCount, 0xFF2E7D32),
                LeadFunnelItem("Lost", lostCount, 0xFFD32F2F),
            )

            _state.value = CallAnalyticsState(
                totalCalls = total,
                incoming = incoming,
                outgoing = outgoing,
                missed = missed,
                totalDurationSec = totalDuration,
                topByCount = topByCount,
                topByDuration = topByDuration,
                hourlyBuckets = hourly,
                weekdayBuckets = weekday,
                answeredRate = answeredRate,
                avgDurationSec = avgDuration,
                deltaTotal = total - prevTotal,
                deltaAnsweredRate = answeredRate - prevAnsweredRate,
                deltaAvgDurationSec = avgDuration - prevAvgDuration,
                followUps = followUps,
                newContacts = newContacts,
                returningContacts = returningContacts,
                shortCalls = shortCalls,
                shortCallRate = shortRate,
                longTalkers = longTalkers,
                busiestHour = busiestHour,
                busiestDay = busiestDay,
                longestCall = longestCall,
                topNewContacts = topNewContacts,
                loading = false,
                rangeLabel = range.label,
                totalLeads = allLeads.size,
                leadsWon = wonCount,
                leadsLost = lostCount,
                leadsInProgress = inProgressCount.coerceAtLeast(0),
                newLeadsInRange = leadsInRange.size,
                leadConversionRate = conversionRate,
                leadFunnel = funnel,
                avgLeadAgeHours = avgAgeHours,
            )
        }
    }

    private fun buildFollowUps(logs: List<CallLogDetails>, windowMillis: Long): List<TopNumber> {
        val cutoff = System.currentTimeMillis() - windowMillis
        val missed = logs.filter { it.type == CallType.MISSED && it.date >= cutoff }
            .groupBy { it.callerId ?: it.number }
        val outgoing = logs.filter { it.type == CallType.OUTGOING }
            .groupBy { it.callerId ?: it.number }

        val pending = missed.mapNotNull { (key, missedCalls) ->
            val hasCallback = outgoing[key]?.any { out -> out.date > (missedCalls.maxOf { it.date }) } ?: false
            if (hasCallback) null else {
                val lastMiss = missedCalls.maxByOrNull { it.date }!!
                TopNumber(
                    number = key ?: "",
                    name = missedCalls.firstNotNullOfOrNull { it.cachedName.takeIf { n -> !n.isNullOrBlank() && n != "Unknown" } },
                    totalCalls = missedCalls.size,
                    totalDuration = 0,
                    lastDate = lastMiss.date
                )
            }
        }
        return pending.sortedByDescending { it.lastDate }.take(10)
    }

    private fun Map.Entry<String?, List<CallLogDetails>>.toTopNumber(byDuration: Boolean = false): TopNumber {
        val calls = value
        val topName = calls.firstOrNull { !it.cachedName.isNullOrBlank() && it.cachedName != "Unknown" }?.cachedName
        val totalDuration = calls.sumOf { it.duration }
        val lastDate = calls.maxOfOrNull { it.date } ?: 0L
        return TopNumber(
            number = key ?: "",
            name = topName,
            totalCalls = calls.size,
            totalDuration = totalDuration,
            lastDate = lastDate
        )
    }

    private fun inRange(date: Long, start: Long?, end: Long?): Boolean {
        val afterStart = start?.let { date >= it } ?: true
        val beforeEnd = end?.let { date <= it } ?: true
        return afterStart && beforeEnd
    }

    private fun resolveRange(range: Range): Pair<Long, Long> {
        val end = range.endMillis ?: System.currentTimeMillis()
        val start = range.startMillis ?: 0L
        return start to end
    }

    private fun previousRange(current: Pair<Long, Long>): Pair<Long, Long> {
        val duration = current.second - current.first
        val prevEnd = current.first
        val prevStart = prevEnd - duration
        return prevStart to prevEnd
    }

    companion object {
        private const val ONE_DAY = 24 * 60 * 60 * 1000L
        private const val THREE_DAYS = 3 * ONE_DAY

        private fun startOfDay(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
