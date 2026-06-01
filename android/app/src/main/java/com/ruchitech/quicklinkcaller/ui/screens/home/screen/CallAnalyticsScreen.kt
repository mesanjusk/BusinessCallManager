package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.CallAnalyticsVm
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.TextPrimary
import com.ruchitech.quicklinkcaller.ui.theme.TextSecondary
import com.ruchitech.quicklinkcaller.ui.theme.NavyElevated
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.normalGoogleSansStyle

@Composable
fun CallAnalyticsScreen(viewModel: CallAnalyticsVm) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderChips(
            currentLabel = state.rangeLabel,
            onRangeSelected = { range ->
                viewModel.refresh(range)
            }
        )

        Spacer(Modifier.height(12.dp))

        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ThemePurple, strokeWidth = 2.dp)
            }
            return
        }

        StatCards(state)
        Spacer(Modifier.height(12.dp))
        Highlights(state)
        Spacer(Modifier.height(12.dp))
        Charts(state)
        Spacer(Modifier.height(12.dp))
        TopContacts(state)
        Spacer(Modifier.height(12.dp))
        ExtraInsights(state)
    }
}

@Composable
private fun HeaderChips(currentLabel: String, onRangeSelected: (CallAnalyticsVm.Range) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            CallAnalyticsVm.Range.Today,
            CallAnalyticsVm.Range.SevenDays,
            CallAnalyticsVm.Range.ThirtyDays,
            CallAnalyticsVm.Range.AllTime
        ).forEach { range ->
            val selected = currentLabel == range.label
            AssistChip(
                onClick = { onRangeSelected(range) },
                label = { Text(range.label, fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) ThemePurple.copy(alpha = 0.15f) else Color(0xFFF6F7FB),
                    labelColor = if (selected) ThemePurple else TextPrimary
                )
            )
        }
    }
}

@Composable
private fun StatCards(state: CallAnalyticsVm.CallAnalyticsState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard("Total", state.totalCalls, ThemePurple, Modifier.weight(1f))
        StatCard("Incoming", state.incoming, Color(0xFF2E8B57), Modifier.weight(1f))
        StatCard("Outgoing", state.outgoing, Color(0xFF1565C0), Modifier.weight(1f))
        StatCard("Missed", state.missed, Color(0xFFD32F2F), Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TrendCard("Answered rate", state.answeredRate, state.deltaAnsweredRate, Modifier.weight(1f))
        TrendCard("Avg duration", state.avgDurationSec / 60f, state.deltaAvgDurationSec / 60f, Modifier.weight(1f), suffix = " min")
        TrendCard("Total vs prev", state.totalCalls.toFloat(), state.deltaTotal.toFloat(), Modifier.weight(1f), isPercent = false, suffix = "")
    }
}

@Composable
private fun StatCard(title: String, value: Int, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = normalGoogleSansStyle, color = TextSecondary)
            Text(
                text = value.toString(),
                style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold),
                fontSize = 20.sp,
                color = tint
            )
        }
    }
}

@Composable
private fun TrendCard(
    title: String,
    value: Float,
    delta: Float,
    modifier: Modifier = Modifier,
    isPercent: Boolean = true,
    suffix: String = "%"
) {
    val trendingUp = delta >= 0
    val deltaText = if (isPercent) String.format("%.1f%%", delta * 100) else String.format("%.0f", delta)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = NavyElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = normalGoogleSansStyle, color = TextSecondary)
            Text(
                text = if (isPercent) String.format("%.0f%%", value * 100) else String.format("%.1f%s", value, suffix),
                style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold),
                fontSize = 16.sp,
                color = TextPrimary
            )
            Text(
                text = "${if (trendingUp) "▲" else "▼"} $deltaText vs prev",
                style = normalGoogleSansStyle,
                fontSize = 12.sp,
                color = if (trendingUp) Color(0xFF2E8B57) else Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
private fun Charts(state: CallAnalyticsVm.CallAnalyticsState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Peak hours", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        BarRow(state.hourlyBuckets, maxValue = state.hourlyBuckets.maxOrNull() ?: 1)
        Text("Days of week", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        BarRow(state.weekdayBuckets, maxValue = state.weekdayBuckets.maxOrNull() ?: 1, labels = listOf("S", "M", "T", "W", "T", "F", "S"))
    }
}

@Composable
private fun BarRow(values: List<Int>, maxValue: Int, labels: List<String>? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { idx, value ->
            val fraction = if (maxValue == 0) 0f else value.toFloat() / maxValue.toFloat()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(NavyElevated, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((70 * fraction).dp)
                            .background(ThemePurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    )
                }
                if (labels != null && labels.size == values.size) {
                    Text(labels[idx], fontSize = 10.sp, color = TextSecondary)
                } else {
                    Text(value.toString(), fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun Highlights(state: CallAnalyticsVm.CallAnalyticsState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val hourText = state.busiestHour?.let { String.format("%02d:00", it) } ?: "–"
        val dayText = state.busiestDay?.let { listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")[it] } ?: "–"
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Best time", style = normalGoogleSansStyle, color = TextSecondary)
                Text("$hourText • $dayText", style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold))
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Longest call", style = normalGoogleSansStyle, color = TextSecondary)
                val lc = state.longestCall
                val longestText = lc?.let {
                    "%s • %.1f min".format(it.name ?: it.number, it.durationSec / 60f)
                } ?: "–"
                Text(
                    text = longestText,
                    style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Follow-ups pending", style = normalGoogleSansStyle, color = TextSecondary)
                Text(
                    text = state.followUps.size.toString(),
                    style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD84315)
                )
            }
        }
    }
}

@Composable
private fun TopContacts(state: CallAnalyticsVm.CallAnalyticsState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Top numbers", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        val items = listOfNotNull(state.topByCount, state.topByDuration).distinctBy { it.number }
        if (items.isEmpty()) {
            Text("No data for this range", style = normalGoogleSansStyle, color = TextSecondary)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NavyElevated
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = item.name ?: item.number,
                                    style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Calls: ${item.totalCalls}  •  Last: ${formatTimeAgo(item.lastDate)}",
                                    style = normalGoogleSansStyle,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${item.totalDuration / 60}m",
                                style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Medium),
                                color = Orange
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Needs follow-up (missed, no callback)", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        if (state.followUps.isEmpty()) {
            Text("All caught up in last 72h", style = normalGoogleSansStyle, color = TextSecondary)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.followUps) { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.name ?: item.number, style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                                Text("Missed ${formatTimeAgo(item.lastDate)} • ${item.totalCalls}x", style = normalGoogleSansStyle, fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("Call back", style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold, color = Orange))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtraInsights(state: CallAnalyticsVm.CallAnalyticsState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("More insights", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("New contacts", state.newContacts, ThemePurple, Modifier.weight(1f))
            StatCard("Returning", state.returningContacts, Color(0xFF1565C0), Modifier.weight(1f))
            StatCard("Short calls", state.shortCalls, Color(0xFFD32F2F), Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TrendCard(
                title = "Short-call rate",
                value = state.shortCallRate,
                delta = 0f,
                modifier = Modifier.weight(1f)
            )
            TrendCard(
                title = "Total talk time",
                value = state.totalDurationSec / 60f,
                delta = 0f,
                modifier = Modifier.weight(1f),
                isPercent = false,
                suffix = " min"
            )
        }

        Text("Long conversations", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        if (state.longTalkers.isEmpty()) {
            Text("No long calls in this range", style = normalGoogleSansStyle, color = TextSecondary)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.longTalkers) { talker ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    talker.name ?: talker.number,
                                    style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                                Text(
                                    "Avg %.1f min • ${talker.calls} calls • Last ${formatTimeAgo(talker.lastDate)}"
                                        .format(talker.avgDurationSec / 60f),
                                    style = normalGoogleSansStyle,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "★",
                                style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold),
                                color = ThemePurple
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("New in this range", style = normalGoogleSansStyle, fontWeight = FontWeight.SemiBold)
        if (state.topNewContacts.isEmpty()) {
            Text("No new contacts in this range", style = normalGoogleSansStyle, color = TextSecondary)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.topNewContacts) { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEDE7F6)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.name ?: item.number, style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                                Text("Since ${formatTimeAgo(item.lastDate)} • ${item.totalCalls} calls", style = normalGoogleSansStyle, fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(
                                text = "${item.totalDuration / 60}m",
                                style = normalGoogleSansStyle.copy(fontWeight = FontWeight.Bold),
                                color = ThemePurple
                            )
                        }
                    }
                }
            }
        }
    }
}
