package com.ruchitech.quicklinkcaller.ui.screens.leads.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.ui.components.*
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.LeadListVm
import com.ruchitech.quicklinkcaller.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadListScreen(viewModel: LeadListVm) {
    val leads by viewModel.leads.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val statuses = listOf(null, "New", "Contacted", "Interested", "Negotiation", "Won", "Lost")

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Leads", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary),
                actions = {
                    IconButton(onClick = { /* pipeline view */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Pipeline", tint = ElectricBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lead")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(statuses) { status ->
                    val isSelected = selectedStatus == status
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (isSelected) ElectricBlue else NavySurface,
                        modifier = Modifier.clickable { viewModel.filterByStatus(status) }
                    ) {
                        Text(
                            text = status ?: "All",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            if (leads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView("No leads yet", "Leads from incoming calls will appear here, or tap + to add manually")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(leads, key = { it.lead_uuid }) { lead ->
                        LeadCard(lead = lead, onClick = { viewModel.openLead(lead.lead_uuid) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLeadDialog(onDismiss = { showAddDialog = false }, onAdd = { phone, name ->
            viewModel.addLead(phone, name)
            showAddDialog = false
        })
    }
}

@Composable
private fun LeadCard(lead: Lead, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarInitials(name = lead.name ?: lead.phone, size = 44)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lead.name ?: "Unknown", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                Text(lead.phone, color = TextSecondary, fontSize = 13.sp)
                if (lead.next_follow_up != null) {
                    Text(
                        "Follow-up: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(lead.next_follow_up))}",
                        color = ElectricBlue, fontSize = 11.sp
                    )
                }
            }
            LeadStatusChip(status = lead.status)
        }
    }
}

@Composable
private fun AddLeadDialog(onDismiss: () -> Unit, onAdd: (String, String?) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("Add Lead", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone Number", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name (optional)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (phone.isNotBlank()) onAdd(phone, name.ifBlank { null }) }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)) {
                Text("Add Lead", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
