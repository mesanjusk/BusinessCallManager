package com.ruchitech.quicklinkcaller.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui.SampleDatePickerView
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.google_sans_medium
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val allCallLogs by viewModel.allCallLogs.collectAsState()
    val whatsappLogs by viewModel.whatsappLogs.collectAsState()
    val whatsappCallLogs by viewModel.whatsappCallLogs.collectAsState()

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val subTabs = listOf("Call Logs", "WhatsApp", "WA Calls")

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        SampleDatePickerView(
            dates = { date1, date2 ->
                val start = date1.toLongOrNull() ?: 0L
                val end = date2.toLongOrNull() ?: System.currentTimeMillis()
                viewModel.applyCustomDate(start, end)
                showDatePicker = false
            },
            onDismissRequest = { showDatePicker = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Date filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityDateFilter.values().forEach { filter ->
                val isSelected = viewModel.selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (filter == ActivityDateFilter.CUSTOM) {
                            showDatePicker = true
                        } else {
                            viewModel.applyFilter(filter)
                        }
                    },
                    label = {
                        Text(
                            filter.label,
                            fontSize = 12.sp.nonScaledSp,
                            fontFamily = montserrat_semibold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleSolid,
                        selectedLabelColor = Color.White,
                    )
                )
            }
        }

        // Sub-tab row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            contentColor = Color.White,
            containerColor = Color.White,
            divider = { Divider(thickness = 0.dp, color = Color.Transparent) },
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = PurpleSolid,
                )
            }
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            title,
                            color = if (pagerState.currentPage == index) PurpleSolid else Color(0xFF333333),
                            fontFamily = google_sans_medium,
                            fontSize = 13.sp.nonScaledSp
                        )
                    }
                )
            }
        }

        HorizontalPager(count = subTabs.size, state = pagerState) { page ->
            when (page) {
                0 -> CallLogsList(
                    logs = allCallLogs,
                    emptyMessage = "No call logs for this period",
                    showCallButton = true,
                    showWhatsAppButton = false
                )
                1 -> CallLogsList(
                    logs = whatsappLogs,
                    emptyMessage = "No WhatsApp activity for this period",
                    showCallButton = false,
                    showWhatsAppButton = true
                )
                2 -> CallLogsList(
                    logs = whatsappCallLogs,
                    emptyMessage = "No WhatsApp calls for this period",
                    showCallButton = false,
                    showWhatsAppButton = true
                )
            }
        }
    }
}

@Composable
private fun CallLogsList(
    logs: List<CallLogDetails>,
    emptyMessage: String,
    showCallButton: Boolean,
    showWhatsAppButton: Boolean,
) {
    val context = LocalContext.current

    if (logs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = emptyMessage,
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    fontFamily = montserrat_semibold
                )
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(logs) { log ->
            ActivityLogItem(
                log = log,
                showCallButton = showCallButton,
                showWhatsAppButton = showWhatsAppButton,
                onCall = { context.makePhoneCall(log.number) },
                onWhatsApp = { context.openWhatsapp(log.number) }
            )
            Divider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
        }
    }
}

@Composable
private fun ActivityLogItem(
    log: CallLogDetails,
    showCallButton: Boolean,
    showWhatsAppButton: Boolean,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
) {
    val typeColor = when (log.type) {
        CallType.INCOMING -> Color(0xFF4CAF50)
        CallType.OUTGOING -> PurpleSolid
        else -> Color(0xFFF44336)
    }
    val typeLabel = when (log.type) {
        CallType.INCOMING -> "Incoming"
        CallType.OUTGOING -> "Outgoing"
        else -> "Missed"
    }
    val displayName = log.cachedName?.takeIf { it.isNotBlank() && it != "Unknown" } ?: log.number
    val dateStr = remember(log.date) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.date))
    }
    val durationStr = remember(log.duration) {
        val mins = log.duration / 60
        val secs = log.duration % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF454545), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.take(1).uppercase(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontFamily = montserrat_semibold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF111111)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(typeLabel, color = typeColor, fontSize = 11.sp.nonScaledSp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
            if (log.duration > 0) {
                Text(
                    text = durationStr,
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }

        if (showCallButton) {
            IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = PurpleSolid,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (showWhatsAppButton) {
            IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_whatsapp),
                    contentDescription = "WhatsApp",
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
