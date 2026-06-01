package com.ruchitech.quicklinkcaller.ui.screens.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.ui.theme.sfMediumFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(viewModel: TeamViewModel) {
    val teamMembers by viewModel.teamMembers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddTeamMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, phone, email, role ->
                viewModel.addTeamMember(name, phone, email, role)
                showAddDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBE9E7))
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateUp() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Team Members",
                fontSize = 16.sp.nonScaledSp,
                fontFamily = montserrat_semibold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Member",
                    tint = PurpleSolid
                )
            }
        }

        if (teamMembers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No team members yet",
                        color = Color(0xFF888888),
                        fontFamily = sfMediumFont,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleSolid)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add First Member", fontFamily = montserrat_semibold)
                    }
                }
            }
            return
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(teamMembers) { memberWithStats ->
                TeamMemberCard(
                    memberWithStats = memberWithStats,
                    onDelete = { viewModel.deleteTeamMember(memberWithStats.member) }
                )
                Divider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            }
        }
    }
}

@Composable
private fun TeamMemberCard(
    memberWithStats: TeamMemberWithStats,
    onDelete: () -> Unit,
) {
    val member = memberWithStats.member
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove Member") },
            text = { Text("Remove ${member.name} from team?") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Remove", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PurpleSolid, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                fontFamily = montserrat_semibold,
                fontSize = 15.sp,
                color = Color(0xFF111111)
            )
            Text(
                text = member.phone,
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
            if (member.role.isNotBlank()) {
                Text(
                    text = member.role,
                    fontSize = 11.sp,
                    color = PurpleSolid
                )
            }
        }

        // Stats
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(PurpleSolid.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${memberWithStats.assignedLeadsCount} leads",
                    fontSize = 12.sp.nonScaledSp,
                    color = PurpleSolid,
                    fontFamily = montserrat_semibold
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color(0xFFEF5350),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeamMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, email: String, role: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Add Team Member",
                    fontSize = 18.sp,
                    fontFamily = montserrat_semibold,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleSolid,
                        focusedLabelColor = PurpleSolid
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleSolid,
                        focusedLabelColor = PurpleSolid
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleSolid,
                        focusedLabelColor = PurpleSolid
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (e.g. Sales, Support)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleSolid,
                        focusedLabelColor = PurpleSolid
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF666666))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAdd(name, phone, email, role) },
                        enabled = name.isNotBlank() && phone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleSolid)
                    ) {
                        Text("Add", fontFamily = montserrat_semibold)
                    }
                }
            }
        }
    }
}
