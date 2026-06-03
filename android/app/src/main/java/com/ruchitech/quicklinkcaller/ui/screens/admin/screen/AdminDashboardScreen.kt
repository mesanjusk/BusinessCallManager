package com.ruchitech.quicklinkcaller.ui.screens.admin.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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
import com.ruchitech.quicklinkcaller.ui.screens.admin.viewmodel.AdminDashboardVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: AdminDashboardVm) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = viewModel::navigateUp) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricBlue, strokeWidth = 2.dp)
            }
            return@Scaffold
        }

        if (state.business == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Group, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Text("No business account found", color = TextSecondary, fontSize = 16.sp)
                    Text("Set up a team first from the Team menu.", color = TextSecondary.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Overview header
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            state.business!!.business_name,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (state.business!!.plan == "premium") Color(0xFFFFD700).copy(alpha = 0.15f)
                                    else ElectricBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (state.business!!.plan == "premium") "✦ Premium" else "Free Plan",
                                color = if (state.business!!.plan == "premium") Color(0xFFFFD700) else ElectricBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OverviewStat("Team Calls", state.totalTeamCalls.toString(), Color(0xFF1976D2), Modifier.weight(1f))
                            OverviewStat("Team Leads", state.totalTeamLeads.toString(), ElectricBlue, Modifier.weight(1f))
                            OverviewStat("Won", state.totalTeamWon.toString(), StatusWon, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Team members section
            if (state.memberStats.isNotEmpty()) {
                item {
                    Text(
                        "Team Performance",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(state.memberStats) { member ->
                    MemberStatCard(member)
                }
            }

            // Recent activity feed
            if (state.recentActivity.isNotEmpty()) {
                item {
                    Text(
                        "Recent Activity",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(state.recentActivity) { activity ->
                    ActivityRow(activity)
                }
            } else {
                item {
                    Text(
                        "No team call activity found. Add member phone numbers in the Team screen.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewStat(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun MemberStatCard(member: AdminDashboardVm.MemberStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.firstOrNull()?.uppercase() ?: "?",
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(member.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        if (member.phone.isNotBlank()) {
                            Text(member.phone, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${member.totalLeads} leads", fontSize = 12.sp, color = ElectricBlue, fontWeight = FontWeight.SemiBold)
                    Text("${member.wonLeads} won", fontSize = 11.sp, color = StatusWon)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CallPill("↙ ${member.incomingCalls}", Color(0xFF1976D2), Modifier.weight(1f))
                CallPill("↗ ${member.outgoingCalls}", Color(0xFF2E7D32), Modifier.weight(1f))
                CallPill("✗ ${member.missedCalls}", Color(0xFFD32F2F), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CallPill(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun ActivityRow(activity: AdminDashboardVm.ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(ElectricBlue.copy(alpha = 0.5f), CircleShape)
        )
        Text(
            text = activity.memberName,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Text(
            text = activity.action,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatTimeAgo(activity.timestamp),
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}
