package com.ruchitech.quicklinkcaller.ui.screens.leads.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.ui.components.*
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.PipelineVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineScreen(viewModel: PipelineVm) {
    val leadsByStage by viewModel.leadsByStage.collectAsState()

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Pipeline", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = viewModel::navigateUp) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier.padding(padding).fillMaxSize().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.width(4.dp))
            viewModel.stages.forEach { stage ->
                val leads = leadsByStage[stage] ?: emptyList()
                PipelineColumn(stage = stage, leads = leads, onMoveStage = { lead, newStage -> viewModel.moveToStage(lead, newStage) }, stages = viewModel.stages)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun PipelineColumn(stage: String, leads: List<Lead>, onMoveStage: (Lead, String) -> Unit, stages: List<String>) {
    val stageColors = mapOf("New" to StatusNew, "Contacted" to StatusContacted, "Interested" to StatusInterested, "Negotiation" to StatusNegotiation, "Won" to StatusWon, "Lost" to StatusLost)
    val color = stageColors[stage] ?: TextDisabled
    Column(
        modifier = Modifier.width(220.dp).fillMaxHeight().background(NavySurface, RoundedCornerShape(16.dp)).padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.2f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(50.dp)))
            Spacer(Modifier.width(8.dp))
            Text(stage, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("${leads.size}", fontSize = 13.sp, color = color)
        }
        LazyColumn(contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leads, key = { it.lead_uuid }) { lead ->
                PipelineLeadCard(lead = lead, onMoveStage = onMoveStage, stages = stages.filter { it != stage })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PipelineLeadCard(lead: Lead, onMoveStage: (Lead, String) -> Unit, stages: List<String>) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyElevated),
        onClick = { showMenu = true }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AvatarInitials(name = lead.name ?: lead.phone, size = 32)
            Spacer(Modifier.height(8.dp))
            Text(lead.name ?: "Unknown", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
            Text(lead.phone, color = TextSecondary, fontSize = 11.sp)
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            stages.forEach { s ->
                DropdownMenuItem(text = { Text("Move to $s", color = TextPrimary) }, onClick = { onMoveStage(lead, s); showMenu = false })
            }
        }
    }
}
