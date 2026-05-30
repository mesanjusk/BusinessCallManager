package com.ruchitech.quicklinkcaller.ui.screens.default_dialer

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp

@Composable
fun SetDefaultDialerScreen(
    viewModel: DefaultDialerViewModel = hiltViewModel(),
    /*    onAgreeClick: () -> Unit,
        onSkipClick: () -> Unit,
        onPrivacyPolicyClick: () -> Unit*/
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Create the launcher
    val dialerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleActivityResult(result)
    }

    // Launch intent when pendingIntent changes
    LaunchedEffect(uiState.pendingIntent) {
        uiState.pendingIntent?.let { intent ->
            dialerLauncher.launch(intent)
        }
    }

    if (uiState.isLoading) {
        // FullScreenLoading()
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }

    Row {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon
                Image(
                    painter = painterResource(R.drawable.ic_app_icon),
                    contentDescription = "Phone Icon",
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(50.dp))

                // Title
                Text(
                    text = "Continue with or without default dialer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "You can still use the app without setting it as your default phone app. You can always change this later in settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons vertically
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Proceed & Skip — primary, large
                    Button(
                        onClick = { viewModel.onSkipClicked() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50), // Green
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Continue")
                    }

                    Spacer(modifier = Modifier.height(60.dp))

                    // Set as Default — smaller and subtle
                    /*
                                        OutlinedButton(
                                            onClick = { viewModel.requestDefaultDialer() },
                                            modifier = Modifier
                                                .height(40.dp)
                                                .padding(4.dp), // smaller height
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // faded text
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) // lighter border
                                            )
                                        ) {
                                            Text(
                                                text = "Set as Default",
                                                style = MaterialTheme.typography.bodySmall // smaller font
                                            )
                                        }
                    */
                }
            }

            // Privacy policy link
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.Underline,
                        color = Color.Blue,
                        fontSize = 14.sp.nonScaledSp
                    ),
                    modifier = Modifier
                        .clickable { viewModel.onPrivacyPolicyClicked() }
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Set as Default",
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.Underline,
                        color = Color.Blue,
                        fontSize = 14.sp.nonScaledSp
                    ),
                    modifier = Modifier
                        .clickable {  viewModel.requestDefaultDialer() }
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}