package com.ruchitech.quicklinkcaller.ui.screens.premium.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.screens.premium.viewmodel.UpgradeVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(viewModel: UpgradeVm) {
    val premiumFeatures = listOf(
        "Unlimited leads — no 100-lead cap",
        "Full call history — no 30-day limit",
        "Team accounts — invite your entire team",
        "Task assignment — delegate to teammates",
        "Advanced analytics — full reports + CSV export",
        "WhatsApp follow-up messages to leads",
        "Priority customer support"
    )

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to Premium", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = viewModel::navigateUp) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            Text("⭐", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text("QuickLink Premium", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            Text("Everything you need to grow your business", fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavySurface), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    premiumFeatures.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = StatusWon, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(feature, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.15f)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("₹999/year", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                    Text("per user", color = TextSecondary, fontSize = 13.sp)
                    Text("Less than ₹84/month", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue), shape = RoundedCornerShape(50.dp)) {
                Text("Upgrade Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text("7-day free trial • Cancel anytime", color = TextDisabled, fontSize = 12.sp)
        }
    }
}
