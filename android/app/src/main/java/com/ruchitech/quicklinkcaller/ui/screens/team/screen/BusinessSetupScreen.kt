package com.ruchitech.quicklinkcaller.ui.screens.team.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.screens.team.viewmodel.TeamManagementVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(viewModel: TeamManagementVm) {
    var businessName by remember { mutableStateOf("") }
    val business by viewModel.business.collectAsState()

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Business Setup", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = viewModel::navigateUp) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Business, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(44.dp))
            }

            Text(
                text = "Set Up Your Business",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Create your business account to manage your team, leads, and tasks from one place.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Business Details", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 16.sp)

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name", color = TextSecondary) },
                        placeholder = { Text("e.g. Acme Corp", color = TextDisabled) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            if (business != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusWon.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Business already set up: ", color = TextSecondary, fontSize = 13.sp)
                        Text(business!!.business_name, color = StatusWon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = { if (businessName.isNotBlank()) viewModel.createBusiness(businessName) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = businessName.isNotBlank()
            ) {
                Text("Create Business Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
