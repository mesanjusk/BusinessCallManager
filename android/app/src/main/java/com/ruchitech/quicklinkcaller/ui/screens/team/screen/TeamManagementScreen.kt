package com.ruchitech.quicklinkcaller.ui.screens.team.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var showAddMemberDialog by remember { mutableStateOf(false) }
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

                // Team members section
                val membersJson = business!!.team_members_json
                val memberCount = membersJson.split("{").size - 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Team Members ($memberCount)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    TextButton(onClick = { showAddMemberDialog = true }) {
                        Text("+ Add Member", color = ElectricBlue, fontSize = 13.sp)
                    }
                }
                if (memberCount == 0) {
                    Text("No members yet. Share the invite code above.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    // Parse simple JSON member list
                    val namePattern = Regex("\"name\":\"([^\"]+)\"")
                    val phonePattern = Regex("\"phone\":\"([^\"]+)\"")
                    val names = namePattern.findAll(membersJson).map { it.groupValues[1] }.toList()
                    val phones = phonePattern.findAll(membersJson).map { it.groupValues[1] }.toList()
                    names.forEachIndexed { i, name ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NavyElevated)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarInitials(name = name, size = 36)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    if (i < phones.size) Text(phones[i], color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    // Add member dialog
    if (showAddMemberDialog) {
        var memberName by remember { mutableStateOf("") }
        var memberPhone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            containerColor = NavySurface,
            title = { Text("Add Team Member", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = memberName, onValueChange = { memberName = it },
                        label = { Text("Member Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memberPhone, onValueChange = { memberPhone = it },
                        label = { Text("Phone Number", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Share the invite code with them to join the app", color = TextSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memberName.isNotBlank()) {
                            viewModel.addMember(memberName, memberPhone)
                            showAddMemberDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) { Text("Add", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showAddMemberDialog = false }) { Text("Cancel", color = TextSecondary) } }
        )
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
