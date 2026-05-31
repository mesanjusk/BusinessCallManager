package com.ruchitech.quicklinkcaller.ui.screens.leads.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.components.*
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.LeadDetailVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(viewModel: LeadDetailVm) {
    val lead by viewModel.lead.collectAsState()
    val stages = listOf("New", "Contacted", "Interested", "Negotiation", "Won", "Lost")

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text(lead?.name ?: "Lead Detail", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = viewModel::navigateUp) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        lead?.let { l ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Hero card
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarInitials(name = l.name ?: l.phone, size = 72)
                        Spacer(Modifier.height(12.dp))
                        Text(l.name ?: "Unknown", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(l.phone, fontSize = 15.sp, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        LeadStatusChip(status = l.status)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue), shape = RoundedCornerShape(50.dp)) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Call Now", color = Color.White)
                        }
                    }
                }

                // Move stage
                SectionHeader("Move Stage")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    stages.forEach { stage ->
                        if (stage != l.status) {
                            SuggestionChip(
                                onClick = { viewModel.updateStatus(stage) },
                                label = { Text(stage, fontSize = 11.sp, color = TextSecondary) }
                            )
                        }
                    }
                }

                // Info card
                SectionHeader("Details")
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailRow("Source", l.source.replaceFirstChar { it.uppercase() })
                        DetailRow("Created", java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(l.created_at)))
                        if (l.next_follow_up != null) DetailRow("Follow-up", java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(l.next_follow_up)))
                    }
                }
            }
        } ?: Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ElectricBlue)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
