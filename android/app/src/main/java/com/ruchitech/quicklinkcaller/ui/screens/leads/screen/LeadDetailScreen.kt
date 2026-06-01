package com.ruchitech.quicklinkcaller.ui.screens.leads.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.AiState
import com.ruchitech.quicklinkcaller.ui.screens.leads.viewmodel.LeadDetailVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(viewModel: LeadDetailVm) {
    val lead by viewModel.lead.collectAsState()
    val stages = listOf("New", "Contacted", "Interested", "Negotiation", "Won", "Lost")
    val aiScoreState by viewModel.aiScoreState.collectAsState()
    val aiMessageState by viewModel.aiMessageState.collectAsState()
    var showMessagePurposeDialog by remember { mutableStateOf(false) }

    if (showMessagePurposeDialog) {
        MessagePurposeDialog(
            onConfirm = { purpose ->
                showMessagePurposeDialog = false
                viewModel.composeWhatsAppMessage(purpose)
            },
            onDismiss = { showMessagePurposeDialog = false }
        )
    }

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        lead?.name ?: "Lead Detail",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::navigateUp) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        lead?.let { l ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarInitials(name = l.name ?: l.phone, size = 72)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            l.name ?: "Unknown",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(l.phone, fontSize = 15.sp, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        LeadStatusChip(status = l.status)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(Icons.Default.Call, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Call Now", color = Color.White)
                        }
                    }
                }

                // Move stage
                SectionHeader("Move Stage")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow("Source", l.source.replaceFirstChar { it.uppercase() })
                        DetailRow(
                            "Created",
                            java.text.SimpleDateFormat(
                                "MMM dd, yyyy",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date(l.created_at))
                        )
                        if (l.next_follow_up != null) {
                            DetailRow(
                                "Follow-up",
                                java.text.SimpleDateFormat(
                                    "MMM dd, yyyy HH:mm",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date(l.next_follow_up))
                            )
                        }
                    }
                }

                // AI Features card
                AiInsightsCard(
                    hasApiKey = viewModel.hasGeminiKey,
                    aiScoreState = aiScoreState,
                    aiMessageState = aiMessageState,
                    onScoreTap = { viewModel.scoreLeadWithAi() },
                    onMessageTap = { showMessagePurposeDialog = true },
                    onDismissScore = { viewModel.resetAiScore() },
                    onDismissMessage = { viewModel.resetAiMessage() },
                )
            }
        } ?: Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ElectricBlue)
        }
    }
}

@Composable
private fun AiInsightsCard(
    hasApiKey: Boolean,
    aiScoreState: AiState,
    aiMessageState: AiState,
    onScoreTap: () -> Unit,
    onMessageTap: () -> Unit,
    onDismissScore: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    SectionHeader("AI Insights")
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!hasApiKey) {
                Text(
                    "Add your Gemini API key in Settings → AI Features to enable AI insights.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                // Lead intelligence score
                AiActionRow(
                    icon = Icons.Default.AutoAwesome,
                    label = "Lead Intelligence Score",
                    state = aiScoreState,
                    buttonLabel = "Analyze",
                    onTap = onScoreTap,
                    onDismiss = onDismissScore,
                )
                Divider(thickness = 0.5.dp, color = NavyElevated)
                // WA message composer
                AiActionRow(
                    icon = Icons.Default.Edit,
                    label = "Compose WhatsApp Message",
                    state = aiMessageState,
                    buttonLabel = "Compose",
                    onTap = onMessageTap,
                    onDismiss = onDismissMessage,
                )
            }
        }
    }
}

@Composable
private fun AiActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    state: AiState,
    buttonLabel: String,
    onTap: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                Text(label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
            when (state) {
                is AiState.Idle -> {
                    Button(
                        onClick = onTap,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(buttonLabel, fontSize = 12.sp, color = Color.White)
                    }
                }
                is AiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ElectricBlue,
                        strokeWidth = 2.dp
                    )
                }
                is AiState.Success, is AiState.Error -> {
                    TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Clear", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
        when (state) {
            is AiState.Success -> {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.08f))
                ) {
                    Text(
                        state.text,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 20.sp
                    )
                }
            }
            is AiState.Error -> {
                Text(
                    "Error: ${state.message}",
                    fontSize = 12.sp,
                    color = Color(0xFFEF5350)
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun MessagePurposeDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val purposes = listOf("Follow-up", "Introduction", "Proposal", "Check-in", "Thank you")
    var selected by remember { mutableStateOf(purposes[0]) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("Message Purpose", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                purposes.forEach { purpose ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selected == purpose,
                            onClick = { selected = purpose },
                            colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
                        )
                        Text(purpose, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("Compose", color = ElectricBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
