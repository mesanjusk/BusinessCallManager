package com.ruchitech.quicklinkcaller.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.DeleteAccountConfirmationDialog
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.sfMediumFont
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUi(viewModel: SettingsVm) {
    val selectedOptions by viewModel.callLogsData.collectAsState()
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    if (showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            onConfirm = {
                showDeleteAccountDialog = false
                viewModel.deleteAccount()
            },
            onCancel = {
                showDeleteAccountDialog = false
            }
        )

    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri ->
        if (uri != null) {
            viewModel.backupDatabase(uri)
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreDatabase(uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        var isServiceEnabled by remember { mutableStateOf(viewModel.appPreference.callerIdType == 0) }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBE9E7))
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateUp() }) {
                Image(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            Text(text = "Settings", fontSize = 18.sp, fontFamily = sfSemibold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Caller ID Card ──
            SettingsCard(title = "CALLER ID DISPLAY") {
                Text("Show caller info for:", fontSize = 13.sp, color = Color(0xFF6B7280), fontFamily = sfMediumFont)
                Spacer(Modifier.height(4.dp))
                CheckBoxOption(
                    text = "Incoming Calls",
                    checked = selectedOptions?.contains(AllCallerIdOptions.Incoming) == true,
                    onCheckedChange = { isChecked ->
                        var t = selectedOptions
                        t = if (isChecked) t?.plus(AllCallerIdOptions.Incoming) else t?.minus(AllCallerIdOptions.Incoming)
                        viewModel.updateCallerIdState(t)
                    }
                )
                CheckBoxOption(
                    text = "Outgoing Calls",
                    checked = selectedOptions?.contains(AllCallerIdOptions.Outgoing) == true,
                    onCheckedChange = { isChecked ->
                        var t = selectedOptions
                        t = if (isChecked) t?.plus(AllCallerIdOptions.Outgoing) else t?.minus(AllCallerIdOptions.Outgoing)
                        viewModel.updateCallerIdState(t)
                    }
                )
                CheckBoxOption(
                    text = "Post Calls",
                    checked = selectedOptions?.contains(AllCallerIdOptions.Post) == true,
                    onCheckedChange = { isChecked ->
                        var t = selectedOptions
                        t = if (isChecked) t?.plus(AllCallerIdOptions.Post) else t?.minus(AllCallerIdOptions.Post)
                        viewModel.updateCallerIdState(t)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                Text("Display mode:", fontSize = 13.sp, color = Color(0xFF6B7280), fontFamily = sfMediumFont)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RadioButtonItem(text = "Popup", isChecked = isServiceEnabled, onCheckedChange = { isServiceEnabled = it })
                    RadioButtonItem(text = "Notification", isChecked = !isServiceEnabled, onCheckedChange = { isServiceEnabled = !it })
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePurple),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { viewModel.updateSettings(selectedOptions, if (!isServiceEnabled) 1 else 0) }
                ) {
                    Text("Save Caller ID Settings")
                }
            }

            // ── AI Features Card ──
            SettingsCard(title = "AI FEATURES") {
                GeminiAiSection(viewModel)
            }

            // ── WhatsApp Card ──
            SettingsCard(title = "WHATSAPP LEAD CAPTURE") {
                WhatsAppLeadCaptureSection()
            }

            // ── Data Card ──
            SettingsCard(title = "DATA MANAGEMENT") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { backupLauncher.launch("quicklink_caller_backup.db") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePurple),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Backup") }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Restore") }
                }
            }

            // ── Account Card ──
            SettingsCard(title = "ACCOUNT") {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B5EA6)),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { viewModel.navigateToRoute(PrepairDataRoute.withArgs("settings")) }
                ) { Text("Check Permissions") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { showDeleteAccountDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350))
                ) { Text("Delete Account") }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontFamily = sfMediumFont,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun GeminiAiSection(viewModel: SettingsVm) {
    val context = LocalContext.current
    val savedKey = viewModel.appPreference.geminiApiKey
    var keyInput by remember { mutableStateOf(savedKey ?: "") }
    var keyVisible by remember { mutableStateOf(false) }
    val hasKey = !savedKey.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "AI Features (Gemini)",
            fontFamily = sfMediumFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add your free Gemini API key to unlock Lead Intelligence Score, WhatsApp Message Composer, Smart Notes Analysis, and Daily Briefing (free — 1,500 req/day).",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // "Get free key" link button
        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4285F4))
        ) {
            Icon(Icons.Default.Key, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Get free API key from Google AI Studio →", fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // API key input
        OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Paste API key here", fontSize = 13.sp) },
            singleLine = true,
            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { keyVisible = !keyVisible }) {
                    Icon(
                        if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ThemePurple,
                unfocusedBorderColor = Color(0xFFD1D5DB)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.saveGeminiApiKey(keyInput) },
                enabled = keyInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Key", fontSize = 13.sp)
            }
            if (hasKey) {
                OutlinedButton(
                    onClick = {
                        keyInput = ""
                        viewModel.saveGeminiApiKey("")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Remove", fontSize = 13.sp, color = Color(0xFFEF5350))
                }
            }
        }

        if (hasKey) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "AI features active",
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun WhatsAppLeadCaptureSection() {
    val context = LocalContext.current
    val isEnabled = NotificationManagerCompat.getEnabledListenerPackages(context)
        .contains(context.packageName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "WhatsApp Lead Capture",
            fontFamily = sfMediumFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isEnabled)
                "✓ Enabled — unknown WhatsApp contacts are auto-added as leads"
            else
                "Auto-capture leads from WhatsApp messages of unknown numbers",
            fontSize = 13.sp,
            color = if (isEnabled) Color(0xFF2E7D32) else Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                context.startActivity(
                    android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) Color(0xFF2E7D32) else ThemePurple
            )
        ) {
            Text(
                text = if (isEnabled) "Manage Notification Access" else "Enable WhatsApp Lead Capture",
                color = Color.White,
                fontSize = 13.sp
            )
        }
        if (!isEnabled) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the button → find Business Call Manager → toggle ON",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun RadioButtonItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onCheckedChange(!isChecked) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isChecked,
            onClick = { onCheckedChange(!isChecked) },
            colors = RadioButtonDefaults.colors(selectedColor = ThemePurple)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(text = text, fontFamily = sfSemibold)
    }
}

@Composable
private fun CheckBoxOption(
    enabled: Boolean = true,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (enabled) {
                    onCheckedChange(!checked)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled = enabled,
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            colors = CheckboxDefaults.colors(checkedColor = ThemePurple)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontFamily = sfSemibold, fontSize = 16.sp)
    }
}

enum class AllCallerIdOptions {
    Incoming,
    Outgoing,
    Post,
    All;

    companion object {
        fun fromString(value: String): AllCallerIdOptions = valueOf(value)
    }
}
