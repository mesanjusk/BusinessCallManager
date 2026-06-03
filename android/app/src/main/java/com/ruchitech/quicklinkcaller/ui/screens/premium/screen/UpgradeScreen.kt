package com.ruchitech.quicklinkcaller.ui.screens.premium.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.ui.screens.premium.viewmodel.UpgradeVm
import com.ruchitech.quicklinkcaller.ui.theme.*

private const val ADMIN_UPI_ID = "ruchitech@upi"
private const val PAYMENT_AMOUNT = "999"
private const val PAYMENT_NOTE = "BusinessCallManager Premium"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeScreen(viewModel: UpgradeVm) {
    val context = LocalContext.current
    val paymentState by viewModel.paymentState.collectAsState()
    var txnId by remember { mutableStateOf("") }
    var showTxnField by remember { mutableStateOf(false) }

    val isPremium = viewModel.isPremium

    val premiumFeatures = listOf(
        "Unlimited leads — no 100-lead cap",
        "Full call history — no 30-day limit",
        "Team accounts — invite your entire team",
        "Task assignment — delegate to teammates",
        "Advanced analytics — full reports + CSV export",
        "WhatsApp follow-up messages to leads",
        "Smart Dialer — AI pre-call briefs",
        "Admin Dashboard — team activity overview",
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            if (isPremium || paymentState is UpgradeVm.PaymentState.Verified) {
                // Already premium
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("★", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("You are Premium!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        Spacer(Modifier.height(6.dp))
                        val expiry = viewModel.appPreference.premiumExpiry
                        if (expiry != null) {
                            val days = ((expiry - System.currentTimeMillis()) / 86400000).coerceAtLeast(0)
                            Text("Subscription valid for $days more days.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                return@Scaffold
            }

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

            when (paymentState) {
                is UpgradeVm.PaymentState.Idle -> {
                    Button(
                        onClick = {
                            // Open UPI payment intent
                            val upiUri = Uri.parse(
                                "upi://pay?pa=$ADMIN_UPI_ID&pn=BusinessCallManager&am=$PAYMENT_AMOUNT&cu=INR&tn=$PAYMENT_NOTE"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // No UPI app installed
                            }
                            viewModel.onPaymentInitiated()
                            showTxnField = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("Pay ₹999 via UPI", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                is UpgradeVm.PaymentState.PendingVerification -> {
                    showTxnField = true
                }
                is UpgradeVm.PaymentState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            (paymentState as UpgradeVm.PaymentState.Error).message,
                            modifier = Modifier.padding(14.dp),
                            color = Color(0xFFEF9A9A),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    showTxnField = true
                }
                else -> Unit
            }

            if (showTxnField && paymentState !is UpgradeVm.PaymentState.Verified) {
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter UPI Transaction ID", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "After completing payment, paste your UPI Transaction ID (UTR number) below to activate Premium.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = txnId,
                            onValueChange = { txnId = it },
                            label = { Text("Transaction ID (UTR)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.submitTransactionId(txnId) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text("Activate Premium", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Your access will be activated immediately with a 7-day trial. Full verification by admin within 24h.",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            if (paymentState is UpgradeVm.PaymentState.Idle) {
                Text("7-day free trial • Cancel anytime", color = TextDisabled, fontSize = 12.sp)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
