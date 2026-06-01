package com.ruchitech.quicklinkcaller.ui.screens.preparingdata

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dawidraszka.composepermissionhandler.core.ExperimentalPermissionHandlerApi
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHost
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHostState
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerResult
import com.dawidraszka.composepermissionhandler.utils.openAppSettings
import com.ruchitech.quicklinkcaller.MainActivity
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.WebViewActivity
import com.ruchitech.quicklinkcaller.helper.Constant
import com.ruchitech.quicklinkcaller.helper.XiaomiUtilities
import com.ruchitech.quicklinkcaller.helper.XiaomiUtilities.isMIUI
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularLoadingIndicator
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.SettingSwitchItem
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_medium
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import kotlinx.coroutines.launch


@Composable
fun CallLogDisclosureDialog(
    context: Context, onAccept: () -> Unit, onDecline: () -> Unit,
) {
    val annotatedText = buildAnnotatedString {
        append("To provide you with advanced call logging features, this app collects and securely uploads your Call Log metadata ")
        append("(including call time, duration, and number) to our server at https://businesscallmanager.onrender.com")
        append("All data is encrypted during transmission and handled with strict confidentiality. ")
        append("We do not collect audio recordings or SMS content. ")
        append("For full details, please review our ")

        // Annotated clickable link text
        pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY_LINK")
        withStyle(
            style = SpanStyle(
                color = Color.Blue, textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }

    AlertDialog(onDismissRequest = onDecline, title = {
        Text("Permission Required")
    }, text = {
        ClickableText(
            text = annotatedText, onClick = { offset ->
                annotatedText.getStringAnnotations("PRIVACY", offset, offset).firstOrNull()?.let {
                    val intent = Intent(context, WebViewActivity::class.java).apply {
                        putExtra("url", Constant.Urls.privacyPolicy)
                        putExtra("type", "Privacy Policy")
                    }
                    context.startActivity(intent)
                }
            })
    }, confirmButton = {
        TextButton(onClick = onAccept) {
            Text("Accept", color = Color.Blue)
        }
    }, dismissButton = {
        TextButton(onClick = onDecline) {
            Text("Decline", color = Color.Red)
        }
    })
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionHandlerApi::class)
@Composable
fun PermissionHandlingUi(viewModel: PrepareDataVm) {
    val loading by viewModel.circularLoadingIndicator.collectAsState()
    var showAlertDialogForSettings by remember {
        mutableStateOf(false)
    }

    var showCallSyncNote by remember {
        mutableStateOf(viewModel.appPreference.oneTimeSyncNoteShow)
    }

    val coroutineScope1 = rememberCoroutineScope()
    val coroutineScope2 = rememberCoroutineScope()
    val coroutineScope3 = rememberCoroutineScope()
    val coroutineScope4 = rememberCoroutineScope()
    val context = LocalContext.current
    val permissionHandlerHostStateCallLogs = PermissionHandlerHostState(
        listOf(Manifest.permission.READ_CALL_LOG)
    )
    val permissionHandlerHostStateContact = PermissionHandlerHostState(
        listOf(Manifest.permission.READ_CONTACTS)
    )

    val permissionHandlerHostStateMakeCalls = PermissionHandlerHostState(
        listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE)
    )
    val permissionHandlerHostStateSendSMS = PermissionHandlerHostState(
        listOf(Manifest.permission.SEND_SMS)
    )

    val permissionHandlerHostStatePostNotification = PermissionHandlerHostState(
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    )

    val requestPermissionLauncher2 =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.appOverOtherGranted.value = Settings.canDrawOverlays(context)
        }
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.batteryOptimization.value = viewModel.isIgnoringBatteryOptimizations()
    }
    remember { mutableStateOf(false) }
    val requestBatteryOptimizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        Log.e("kgjhbuyi", "PermissionHandlingUi: ${result.data}")/*        val settingsIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                val intentUri = Uri.fromParts("package", context.packageName, null)
                settingsIntent.data = intentUri
                val activityIntent = Intent(Intent.ACTION_VIEW, settingsIntent.data)
                context.startActivity(activityIntent)*/
        viewModel.batteryOptimization.value = viewModel.isIgnoringBatteryOptimizations()
        //batteryOptimizationEnabled.value = Settings.
    }


    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        Log.e("fhbgvfdrxh", "$isGranted: ")
        when (viewModel.permissionType.value) {
            1 -> {
                viewModel.callLogPermission.value = isGranted
            }

            2 -> {
                viewModel.contactsPermission.value = isGranted
            }

            3 -> {
                viewModel.makeCallPermission.value = isGranted
            }

            4 -> {
                viewModel.sendSMSPermission.value = isGranted
            }
        }
    }

   /* if (showCallSyncNote) {
        CallLogDisclosureDialog(context, onAccept = {
            showCallSyncNote = false
            viewModel.appPreference.oneTimeSyncNoteShow = true
        }) {
            showCallSyncNote = true
            viewModel.appPreference.oneTimeSyncNoteShow = false
            (context as MainActivity).finish()
        }
    }*/


    PermissionHandlerHost(
        hostState = permissionHandlerHostStateCallLogs,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(
                        text = "Permission Required!",
                        color = TextColor,
                        fontSize = 14.sp.nonScaledSp,
                        fontFamily = montserrat_semibold
                    )
                },
                text = {
                    Text(
                        "This permission is required. Please grant the permission on the next screen.",
                        color = TextColor,
                        fontSize = 12.sp.nonScaledSp,
                    )
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok", color = ThemePurple, fontSize = 12.sp.nonScaledSp)
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel", color = TextColor, fontSize = 12.sp.nonScaledSp)
                    }
                })
        })

    PermissionHandlerHost(
        hostState = permissionHandlerHostStateContact,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(
                        text = "Permission Required!",
                        color = TextColor,
                        fontSize = 14.sp.nonScaledSp,
                        fontFamily = montserrat_semibold
                    )
                },
                text = {
                    Text(
                        "This permission is required. Please grant the permission on the next screen.",
                        color = TextColor,
                        fontSize = 12.sp.nonScaledSp
                    )
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok", color = ThemePurple, fontSize = 12.sp.nonScaledSp)
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel", color = TextColor, fontSize = 12.sp.nonScaledSp)
                    }
                })
        })
    PermissionHandlerHost(
        hostState = permissionHandlerHostStateMakeCalls,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(
                        text = "Permission Required!",
                        color = TextColor,
                        fontSize = 14.sp.nonScaledSp,
                        fontFamily = montserrat_semibold
                    )
                },
                text = {
                    Text(
                        "This permission is required. Please grant the permission on the next screen.",
                        color = TextColor,
                        fontSize = 12.sp.nonScaledSp
                    )
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok", color = ThemePurple, fontSize = 14.sp.nonScaledSp)
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel", color = TextColor, fontSize = 12.sp.nonScaledSp)
                    }
                })
        })

    PermissionHandlerHost(
        hostState = permissionHandlerHostStateSendSMS,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(
                        text = "Permission Required!",
                        color = TextColor,
                        fontSize = 14.sp.nonScaledSp,
                        fontFamily = montserrat_semibold
                    )
                },
                text = {
                    Text(
                        "This permission is required. Please grant the permission on the next screen.",
                        color = TextColor,
                        fontSize = 12.sp.nonScaledSp
                    )
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok", color = ThemePurple, fontSize = 12.sp.nonScaledSp)
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel", color = TextColor, fontSize = 12.sp.nonScaledSp)
                    }
                })
        })

    PermissionHandlerHost(
        hostState = permissionHandlerHostStatePostNotification,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(
                        text = "Permission Required!",
                        color = TextColor,
                        fontSize = 14.sp.nonScaledSp,
                        fontFamily = montserrat_semibold
                    )
                },
                text = {
                    Text(
                        "This permission is required. Please grant the permission on the next screen.",
                        color = TextColor,
                        fontSize = 12.sp.nonScaledSp
                    )
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok", color = ThemePurple, fontSize = 12.sp.nonScaledSp)
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel", color = TextColor, fontSize = 12.sp.nonScaledSp)
                    }
                })
        })


    if (showAlertDialogForSettings) {
        AlertDialog(icon = {
            Icon(Icons.Default.Lock, contentDescription = "Example Icon")
        }, title = {
            Text(
                text = "Permissions Required!",
                color = TextColor,
                fontSize = 14.sp.nonScaledSp,
                fontFamily = montserrat_semibold
            )
        }, text = {
            Text(
                text = "To provide a personalized  app core feature experience, please grant the Call Log permission.",
                color = TextColor,
                fontSize = 12.sp.nonScaledSp
            )
        }, onDismissRequest = {
            showAlertDialogForSettings = false
        }, confirmButton = {
            TextButton(
                onClick = {
                    showAlertDialogForSettings = false
                    openAppSettings(context)
                }) {
                Text("Goto Settings", color = ThemePurple, fontSize = 14.sp.nonScaledSp)
            }
        }, dismissButton = {
            TextButton(
                onClick = {
                    showAlertDialogForSettings = false
                }) {
                Text("Dismiss", color = TextColor, fontSize = 14.sp.nonScaledSp)
            }
        })
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(NavyPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(start = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Permissions Required",
                    fontSize = 14.sp.nonScaledSp,
                    fontFamily = montserrat_semibold
                )
            }

            Card(
                modifier = Modifier.padding(10.dp), colors = CardDefaults.cardColors(
                    containerColor = Color(
                        0xFFEEEEF0
                    ), disabledContainerColor = Color(0xFFF7F7F8)
                ), elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    text = "To provide you Call Popup notification information ,please grant the following \npermission",
                    fontSize = 12.sp.nonScaledSp,
                    color = Color(0xFFA0A0A5),
                    textAlign = TextAlign.Center,
                    fontFamily = montserrat_medium,
                    lineHeight = 20.sp.nonScaledSp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 18.dp)
                )

            }

            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionType(
                        name = "Call logs",
                        iconId = R.drawable.call_log_black,
                        viewModel.callLogPermission.value
                    ) {
                        viewModel.permissionType.value = 1
                        coroutineScope1.launch {
                            when (permissionHandlerHostStateCallLogs.handlePermissions()) {
                                PermissionHandlerResult.DENIED -> {
                                    //App permission denied
                                    showAlertDialogForSettings = true
                                }

                                PermissionHandlerResult.GRANTED -> {
                                    viewModel.callLogPermission.value = true
                                }

                                PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                                    Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Divider(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        color = Color(
                            0xFFEBEAEA
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionType(
                        name = "Contacts",
                        iconId = R.drawable.ic_contact_black,
                        viewModel.contactsPermission.value
                    ) {
                        viewModel.permissionType.value = 2
                        Log.e("jhjhjghj", "PermissionHandlingUi: 239")
                        coroutineScope2.launch {
                            when (permissionHandlerHostStateContact.handlePermissions()) {
                                PermissionHandlerResult.DENIED -> {
                                    //App permission denied
                                    showAlertDialogForSettings = true
                                }

                                PermissionHandlerResult.GRANTED -> {
                                    viewModel.contactsPermission.value = true
                                }

                                PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                                    Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Divider(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        color = Color(
                            0xFFEBEAEA
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionType(
                        name = "Make phone calls",
                        iconId = R.drawable.ic_phone_call,
                        viewModel.makeCallPermission.value
                    ) {
                        viewModel.permissionType.value = 3
                        coroutineScope3.launch {
                            when (permissionHandlerHostStateMakeCalls.handlePermissions()) {
                                PermissionHandlerResult.DENIED -> {
                                    //App permission denied
                                    showAlertDialogForSettings = true
                                }

                                PermissionHandlerResult.GRANTED -> {
                                    viewModel.makeCallPermission.value = true
                                }

                                PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                                    Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                                }
                            }
                        }

                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Divider(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        color = Color(
                            0xFFEBEAEA
                        )
                    )/*                   Spacer(modifier = Modifier.height(10.dp))
                                       PermissionType(
                                           name = "Send SMS",
                                           iconId = R.drawable.ic_send_sms,
                                           viewModel.sendSMSPermission.value
                                       ) {
                                           viewModel.permissionType.value = 4
                                           coroutineScope4.launch {
                                               when (permissionHandlerHostStateSendSMS.handlePermissions()) {
                                                   PermissionHandlerResult.DENIED -> {
                                                       //App permission denied
                                                       showAlertDialogForSettings = true
                                                   }

                                                   PermissionHandlerResult.GRANTED -> {
                                                       viewModel.sendSMSPermission.value = true
                                                   }

                                                   PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                                                       Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                                                   }
                                               }

                                           }

                                       }*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Divider(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp), color = Color(
                                0xFFEBEAEA
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionType(
                            name = "Notification permission",
                            iconId = R.drawable.ic_notification_black,
                            viewModel.postNotificationPermission.value
                        ) {
                            viewModel.permissionType.value = 4
                            coroutineScope4.launch {
                                when (permissionHandlerHostStatePostNotification.handlePermissions()) {
                                    PermissionHandlerResult.DENIED -> {
                                        //App permission denied
                                        showAlertDialogForSettings = true
                                    }

                                    PermissionHandlerResult.GRANTED -> {
                                        viewModel.postNotificationPermission.value = true
                                    }

                                    PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                                        Log.e(
                                            "mkjfhgfg", "PermissionHandlingUi: permanently denied"
                                        )
                                    }
                                }

                            }

                        }

                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Divider(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        color = Color(
                            0xFFEBEAEA
                        )
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
            Card(
                modifier = Modifier.padding(10.dp), colors = CardDefaults.cardColors(
                    containerColor = Color(
                        0xFFEEEEF0
                    ), disabledContainerColor = Color(0xFFF7F7F8)
                ), elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    text = "To enable Call Popup Notification overlay, please grant the “Display over the others apps” permission",
                    fontSize = 12.sp.nonScaledSp,
                    lineHeight = 20.sp.nonScaledSp,
                    color = Color(0xFFA0A0A5),
                    textAlign = TextAlign.Center,
                    fontFamily = montserrat_medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 18.dp)
                )

            }

            Card(
                modifier = Modifier.padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                PermissionType(
                    name = "Call Popup Notification Overlay Permission",
                    iconId = R.drawable.ic_user_black,
                    viewModel.appOverOtherGranted.value
                ) {
                    XiaomiUtilities()
                    if (isMIUI()) {
                        val intent: Intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                        intent.setClassName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity"
                        )
                        intent.putExtra("extra_pkgname", context.packageName)
                        context.startActivity(intent)
                    } else {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        requestPermissionLauncher2.launch(intent)
                    }

                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(0.dp))
            Card(
                modifier = Modifier.padding(10.dp), colors = CardDefaults.cardColors(
                    containerColor = Color(
                        0xFFEEEEF0
                    ), disabledContainerColor = Color(0xFFF7F7F8)
                ), elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    text = "To enable smooth experience, please enable battery optimization for our app in your device settings.",
                    fontSize = 12.sp.nonScaledSp,
                    lineHeight = 20.sp.nonScaledSp,
                    color = Color(0xFFA0A0A5),
                    textAlign = TextAlign.Center,
                    fontFamily = montserrat_medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 18.dp)
                )

            }

            Spacer(modifier = Modifier.height(0.dp))
            Card(
                modifier = Modifier.padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                PermissionType(
                    name = "Battery optimization",
                    iconId = R.drawable.ic_batteryopt_black,
                    viewModel.batteryOptimization.value
                ) {
                    /*      val intent = Intent(
                              "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"
                          )*/

                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    requestBatteryOptimizationLauncher.launch(intent)
                    //requestPermissionLauncher3.launch(intent)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    viewModel.startDataProcessing()
                },
                shape = RoundedCornerShape(percent = 18),
                elevation = ButtonDefaults.buttonElevation(5.dp),
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)
                    .shadow(shape = RoundedCornerShape(percent = 0), elevation = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemePurple)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "FINISH",
                        color = Color.White,
                        fontSize = 18.sp.nonScaledSp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = montserrat_medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        if (loading) CircularLoadingIndicator()
    }

}

@Composable
private fun PermissionType(
    name: String, iconId: Int, isEnabled: Boolean = false, onButtonClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Box(modifier = Modifier.weight(2F)) {
            Text(
                text = name,
                fontFamily = montserrat_semibold,
                fontSize = 14.sp.nonScaledSp,
                color = TextColor,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .align(alignment = Alignment.CenterStart)
                    .fillMaxWidth()
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            SettingSwitchItem(
                title = R.string.app_name,
                description = R.string.app_name,
                checked = isEnabled,
                onCheckedChange = { newState ->
                    //item1EnabledState = newState
                    onButtonClick()
                })
        }
    }

}