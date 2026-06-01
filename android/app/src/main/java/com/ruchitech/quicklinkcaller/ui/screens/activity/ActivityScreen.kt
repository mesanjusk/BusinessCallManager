package com.ruchitech.quicklinkcaller.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui.SampleDatePickerView
import com.ruchitech.quicklinkcaller.ui.theme.DividerColor
import com.ruchitech.quicklinkcaller.ui.theme.ElectricBlue
import com.ruchitech.quicklinkcaller.ui.theme.NavyElevated
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.TextPrimary
import com.ruchitech.quicklinkcaller.ui.theme.TextSecondary
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
    val whatsappMessages by viewModel.whatsappMessages.collectAsState()
    val whatsappCalls by viewModel.whatsappCalls.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPrimary)
    ) {
        // Date filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
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
                        selectedContainerColor = ElectricBlue,
                        selectedLabelColor = Color.White,
                        labelColor = TextSecondary,
                        containerColor = NavyElevated,
                    )
                )
            }
        }

        // Sub-tab row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            contentColor = TextPrimary,
            containerColor = NavySurface,
            divider = { Divider(thickness = 0.dp, color = Color.Transparent) },
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = ElectricBlue,
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
                            color = if (pagerState.currentPage == index) ElectricBlue else TextSecondary,
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
                    emptyMessage = "No call logs for this period"
                )
                1 -> WhatsAppLeadsList(
                    leads = whatsappMessages,
                    emptyMessage = "No WhatsApp messages for this period",
                    isCall = false
                )
                2 -> WhatsAppLeadsList(
                    leads = whatsappCalls,
                    emptyMessage = "No WhatsApp calls for this period",
                    isCall = true
                )
            }
        }
    }
}

@Composable
private fun CallLogsList(
    logs: List<CallLogDetails>,
    emptyMessage: String,
) {
    val context = LocalContext.current

    if (logs.isEmpty()) {
        EmptyState(icon = { Icon(Icons.Default.Call, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(56.dp)) }, message = emptyMessage)
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(NavyPrimary)) {
        items(logs) { log ->
            CallLogItem(
                log = log,
                onCall = { context.makePhoneCall(log.number) },
                onWhatsApp = { context.openWhatsapp(log.number) }
            )
            Divider(thickness = 0.5.dp, color = DividerColor)
        }
    }
}

@Composable
private fun WhatsAppLeadsList(
    leads: List<Lead>,
    emptyMessage: String,
    isCall: Boolean,
) {
    val context = LocalContext.current

    if (leads.isEmpty()) {
        val icon: @Composable () -> Unit = {
            Icon(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(56.dp)
            )
        }
        EmptyState(icon = icon, message = emptyMessage)
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(NavyPrimary)) {
        items(leads) { lead ->
            WhatsAppLeadItem(
                lead = lead,
                isCall = isCall,
                onWhatsApp = { context.openWhatsapp(lead.phone) }
            )
            Divider(thickness = 0.5.dp, color = DividerColor)
        }
    }
}

@Composable
private fun EmptyState(icon: @Composable () -> Unit, message: String) {
    Box(modifier = Modifier.fillMaxSize().background(NavyPrimary), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = TextSecondary, fontSize = 14.sp, fontFamily = montserrat_semibold)
        }
    }
}

@Composable
private fun CallLogItem(
    log: CallLogDetails,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
) {
    val typeColor = when (log.type) {
        CallType.INCOMING -> Color(0xFF4CAF50)
        CallType.OUTGOING -> ElectricBlue
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
            .background(NavySurface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(NavyElevated, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(displayName.take(1).uppercase(), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(displayName, fontFamily = montserrat_semibold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(typeLabel, color = typeColor, fontSize = 11.sp.nonScaledSp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(dateStr, fontSize = 11.sp, color = TextSecondary)
            }
            if (log.duration > 0) {
                Text(durationStr, fontSize = 11.sp, color = TextSecondary)
            }
        }
        IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = ElectricBlue, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
            Icon(painterResource(id = R.drawable.ic_whatsapp), contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun WhatsAppLeadItem(
    lead: Lead,
    isCall: Boolean,
    onWhatsApp: () -> Unit,
) {
    val displayName = lead.name?.takeIf { it.isNotBlank() } ?: lead.phone
    val dateStr = remember(lead.created_at) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(lead.created_at))
    }
    val badgeLabel = if (isCall) "WA Call" else "WA Message"
    val badgeColor = Color(0xFF25D366)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavySurface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = null,
                tint = Color(0xFF25D366),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(displayName, fontFamily = montserrat_semibold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badgeLabel, color = badgeColor, fontSize = 11.sp.nonScaledSp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(dateStr, fontSize = 11.sp, color = TextSecondary)
            }
            if (lead.status.isNotBlank() && lead.status != "New") {
                Text("Status: ${lead.status}", fontSize = 11.sp, color = TextSecondary)
            }
        }
        IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
            Icon(painterResource(id = R.drawable.ic_whatsapp), contentDescription = "Open WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(22.dp))
        }
    }
}
