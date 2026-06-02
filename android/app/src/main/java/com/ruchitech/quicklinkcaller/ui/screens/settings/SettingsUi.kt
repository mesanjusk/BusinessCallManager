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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
        var isServiceEnabled by remember { mutableStateOf(viewModel.appPreference.callerIdType==0) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(
                        0xFFFBE9E7
                    )
                )
                .height(50.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.navigateUp()
            }) {
                Image(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Settings", fontSize = 15.sp.nonScaledSp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Select Caller ID Options", fontFamily = sfMediumFont, fontSize = 20.sp)
            // CheckBox for Incoming Calls
            CheckBoxOption(
                enabled = true,
                text = "Incoming Calls",
                checked = selectedOptions?.contains(AllCallerIdOptions.Incoming) == true,
                onCheckedChange = { isChecked ->
                    var tempData = selectedOptions
                    tempData = if (isChecked) {
                        tempData?.plus(AllCallerIdOptions.Incoming)
                    } else {
                        tempData?.minus(AllCallerIdOptions.Incoming)
                    }
                    viewModel.updateCallerIdState(tempData)
                }
            )

            // CheckBox for Outgoing Calls
            CheckBoxOption(
                enabled = true,
                text = "Outgoing Calls",
                checked = selectedOptions?.contains(AllCallerIdOptions.Outgoing) == true,
                onCheckedChange = { isChecked ->
                    var tempData = selectedOptions
                    tempData = if (isChecked) {
                        tempData?.plus(AllCallerIdOptions.Outgoing)
                    } else {
                        tempData?.minus(AllCallerIdOptions.Outgoing)
                    }
                    viewModel.updateCallerIdState(tempData)
                }
            )

            // CheckBox for Post Calls
            CheckBoxOption(
                enabled = true,
                text = "Post Calls",
                checked = selectedOptions?.contains(AllCallerIdOptions.Post) == true,
                onCheckedChange = { isChecked ->
                    var tempData = selectedOptions
                    tempData = if (isChecked) {
                        tempData?.plus(AllCallerIdOptions.Post)
                    } else {
                        tempData?.minus(AllCallerIdOptions.Post)
                    }
                    viewModel.updateCallerIdState(tempData)
                }
            )
            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Select mode of  the Caller ID service to control the display of incoming call information.",
                fontFamily = sfMediumFont, fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButtonItem(
                    text = "Popup",
                    isChecked = isServiceEnabled,
                    onCheckedChange = { isServiceEnabled = it }
                )

                RadioButtonItem(
                    text = "Notification",
                    isChecked = !isServiceEnabled,
                    onCheckedChange = { isServiceEnabled = !it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePurple),
                onClick = {
                    val type = if (!isServiceEnabled) 1 else 0

                        viewModel.updateSettings(selectedOptions,type)
                    /*    viewModel.enableDisableCallerIdService(isServiceEnabled)*/

                //    viewModel.updateCallerIdType(type)

                }) {
                Text(text = "Save")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    onClick = {
                        viewModel.navigateToRoute(PrepairDataRoute.withArgs("settings"))
                    }) {
                    Text(text = "Check Permissions")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    onClick = {
                       showDeleteAccountDialog = true
                    }) {
                    Text(text = "Delete Account")
                }
            }



            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePurple),
                    onClick = {
                        backupLauncher.launch("quicklink_caller_backup.db")
                    }) {
                    Text(text = "Backup Data")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePurple),
                    onClick = {
                        restoreLauncher.launch(arrayOf("*/*"))
                    }) {
                    Text(text = "Restore Data")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            GeminiAiSection(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            WhatsAppLeadCaptureSection()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                fontFamily = sfMediumFont,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Caller ID Settings:\n")
                    }
                    append("\nCustomize your Caller ID preferences to tailor the information you want to see. Choose from the following options:\n\n")
                    append("1. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Incoming Calls:")
                    }
                    append(" Receive Caller ID information for incoming calls only.\n\n")
                    append("2. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Outgoing Calls:")
                    }
                    append(" View Caller ID details for outgoing calls exclusively.\n\n")
                    append("3. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Post Calls:")
                    }
                    append(" Access Caller ID details after completing a call.\n\n")
                    /*                 append("4. ")
                                     withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                         append("All Caller ID Types:")
                                     }
                                     append(" Enable Caller ID for all types of calls.\n\n")*/
                    append("Select multiple options to personalize your Caller ID experience. Save your preferences to ensure you see the information that matters most to you.")
                },
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(16.dp)
            )
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
