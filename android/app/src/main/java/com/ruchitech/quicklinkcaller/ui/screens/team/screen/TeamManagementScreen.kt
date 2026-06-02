package com.ruchitech.quicklinkcaller.ui.screens.team.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
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
fun TeamManagementScreen(viewModel: TeamManagementVm, showBackButton: Boolean = false) {
    val business by viewModel.business.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    val namePattern = remember { Regex("\"name\":\"([^\"]+)\"") }
    val phonePattern = remember { Regex("\"phone\":\"([^\"]+)\"") }

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Team", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp) },
                navigationIcon = if (showBackButton) {
                    { IconButton(onClick = viewModel::navigateUp) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } }
                } else { {} },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        },
        floatingActionButton = {
            if (business != null) {
                FloatingActionButton(
                    onClick = { showAddMemberDialog = true },
                    containerColor = ElectricBlue,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (business == null) {
                item {
                    // No business yet — setup card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(ElectricBlue.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Group, null, tint = ElectricBlue, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Set Up Your Team",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Create a business account to invite teammates and manage leads together.",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Create Business Account", color = Color.White)
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { showJoinDialog = true },
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                            ) {
                                Text("Join with Invite Code")
                            }
                        }
                    }
                }
                item { PremiumBadge(modifier = Modifier.fillMaxWidth()) }
            } else {
                val b = business!!
                val names = namePattern.findAll(b.team_members_json).map { it.groupValues[1] }.toList()
                val phones = phonePattern.findAll(b.team_members_json).map { it.groupValues[1] }.toList()

                // Business header card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(b.business_name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (b.plan == "premium") Color(0xFFFFD700).copy(alpha = 0.15f)
                                               else ElectricBlue.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = if (b.plan == "premium") "✦ Premium" else "Free Plan",
                                            color = if (b.plan == "premium") Color(0xFFFFD700) else ElectricBlue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                // Stats chip
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${names.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                                    Text("members", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = DividerColor)

                            Text("Invite Code", color = TextSecondary, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(NavyElevated, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    b.invite_code,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    fontSize = 26.sp,
                                    letterSpacing = 6.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(b.invite_code)) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Share this code with teammates to join", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // Team members header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Team Members (${names.size})",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                }

                if (names.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NavySurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No members yet", color = TextSecondary, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Share the invite code above to add teammates.", color = TextSecondary.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(names.size) { i ->
                        TeamMemberCard(
                            name = names[i],
                            phone = phones.getOrNull(i) ?: ""
                        )
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        var memberName by remember { mutableStateOf("") }
        var memberPhone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            containerColor = NavySurface,
            title = { Text("Add Team Member", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = memberName, onValueChange = { memberName = it },
                        label = { Text("Member Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memberPhone, onValueChange = { memberPhone = it },
                        label = { Text("Phone Number", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("They'll also need the invite code to join the app.", color = TextSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { if (memberName.isNotBlank()) { viewModel.addMember(memberName, memberPhone); showAddMemberDialog = false } },
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
            title = { Text("Create Business Account", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = bizName, onValueChange = { bizName = it },
                    label = { Text("Business Name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { if (bizName.isNotBlank()) { viewModel.createBusiness(bizName); showCreateDialog = false } },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) { Text("Create", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = TextSecondary) } }
        )
    }

    if (showJoinDialog) {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            containerColor = NavySurface,
            title = { Text("Join a Business", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = code, onValueChange = { code = it.uppercase().take(6) },
                    label = { Text("6-character Invite Code", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { if (code.length == 6) { viewModel.joinBusiness(code); showJoinDialog = false } },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) { Text("Join", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showJoinDialog = false }) { Text("Cancel", color = TextSecondary) } }
        )
    }
}

@Composable
private fun TeamMemberCard(name: String, phone: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitials(name = name, size = 44)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (phone.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(phone, color = TextSecondary, fontSize = 13.sp)
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = ElectricBlue.copy(alpha = 0.1f)
            ) {
                Text(
                    "Member",
                    color = ElectricBlue,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
