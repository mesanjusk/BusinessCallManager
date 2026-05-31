package com.ruchitech.quicklinkcaller.ui.screens.team.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.components.*
import com.ruchitech.quicklinkcaller.ui.screens.team.viewmodel.TeamManagementVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(viewModel: TeamManagementVm) {
    val business by viewModel.business.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Team", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = viewModel::navigateUp) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (business == null) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavySurface), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, null, tint = ElectricBlue, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Create a Business Account", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                        Text("Invite your team and manage leads together", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showCreateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue), shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("Create Business", color = Color.White)
                        }
                    }
                }
                PremiumBadge(modifier = Modifier.align(Alignment.End))
            } else {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavySurface), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(business!!.business_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
                        Text(if (business!!.plan == "premium") "Premium Plan" else "Free Plan", color = if (business!!.plan == "premium") Color(0xFFFFD700) else TextSecondary, fontSize = 13.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DividerColor)
                        Text("Invite Code", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(business!!.invite_code, fontWeight = FontWeight.Bold, color = ElectricBlue, fontSize = 24.sp, letterSpacing = 4.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { clipboard.setText(AnnotatedString(business!!.invite_code)) }) {
                                Icon(Icons.Default.ContentCopy, null, tint = ElectricBlue)
                            }
                        }
                        Text("Share this code with teammates to join", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var bizName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = NavySurface,
            title = { Text("Create Business", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(value = bizName, onValueChange = { bizName = it }, label = { Text("Business Name", color = TextSecondary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = { viewModel.createBusiness(bizName); showCreateDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = TextSecondary) } }
        )
    }
}
