package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import SaveContactUi
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.WebViewActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.ruchitech.quicklinkcaller.helper.Constant
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.formatDateFromMillis
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularLoadingIndicator
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TriStateToggle
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui.SampleDatePickerView
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm
import com.ruchitech.quicklinkcaller.ui.screens.notesandreminders.viewmodel.NoteAndReminderVm
import com.ruchitech.quicklinkcaller.ui.theme.NavyElevated
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.TextPrimary
import com.ruchitech.quicklinkcaller.ui.theme.TextSecondary
import com.ruchitech.quicklinkcaller.ui.theme.google_sans_medium
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.ui.theme.normalGoogleSansStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        color = TextSecondary.copy(alpha = 0.6f),
        fontFamily = montserrat_semibold,
        modifier = androidx.compose.ui.Modifier.padding(start = 16.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun DefaultDialerMenuItem(
    context: Context = LocalContext.current,
    onDismissRequest: () -> Unit,
    onClick: () -> Unit,
) {
    val telecomManager = remember {
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    }

    val isDefaultDialer = remember {
        telecomManager.defaultDialerPackage == context.packageName
    }

    DropdownMenuItem(
        onClick = {
            onDismissRequest()
            if (isDefaultDialer) {
                // Open system settings for default apps
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                context.startActivity(intent)
            } else {
                onClick()
                /*     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                         val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                         context.startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
                     } else {
                         context.startActivity(
                             Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                                 putExtra(
                                     TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                                     context.packageName
                                 )
                             }
                         )
                     }*/
            }
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Default Dialer",
                    fontFamily = montserrat_semibold
                )
                Spacer(Modifier.width(8.dp))
                if (isDefaultDialer) {
                    Badge(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    ) {
                        Text("ON", fontSize = 10.sp)
                    }
                } else {
                    Badge(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    ) {
                        Text("OFF", fontSize = 10.sp)
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreen(viewModel: HomeVm) {
    val noteAndReminderVm: NoteAndReminderVm = hiltViewModel()
    val circularLoadingIndicator by viewModel.circularLoadingIndicator.collectAsState()
    val pagerState = com.google.accompanist.pager.rememberPagerState()
    var showSaveInappDialog by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val tabs = listOf(
        TabItem.NotesTab(noteAndReminderVm),
        TabItem.CallLogTab(viewModel),
        TabItem.ShowContactsTab(viewModel),
    )
    var expanded by remember {
        mutableStateOf(false)
    }
    var showDateRangePicker by remember {
        mutableStateOf(false)
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

    LaunchedEffect(Unit) {
        pagerState.scrollToPage(1)
        delay(1000)
        if (viewModel.tabNumber.intValue != 8) {
            //   pagerState.animateScrollToPage(viewModel.tabNumber.intValue)
        }
    }

    if (showDateRangePicker) {
        SampleDatePickerView(dates = { date1, date2 ->
            viewModel.fetchCallLogsByDate(date1, date2)
            showDateRangePicker = false
        }, onDismissRequest = {
            showDateRangePicker = false
        })
    }

    if (showSaveInappDialog) {
        Dialog(onDismissRequest = { showSaveInappDialog = false }) {
            SaveContactUi("", onClose = {
                showSaveInappDialog = false
            }, onSave = { name, number ->
                showSaveInappDialog = false
                EventEmitter.postEvent(
                    Event.HomeVm(
                        1,
                        Contact(
                            contact_title = name,
                            contact_mobile = number,
                            email = "",
                            address = "",
                            created_at = formatDateFromMillis(Date().time)
                        )
                    )
                )
            }, onFocusChangesForName = {
            }, contactHelper = viewModel.contactHelper)

        }
    }


    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = NavyPrimary)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomSearchBar(
                    query = if (pagerState.currentPage == 1) viewModel.query.value else viewModel.searchContactsQuery.value,
                    onQueryChange = { newQuery ->
                        if (pagerState.currentPage == 1) {
                            viewModel.query.value = newQuery
                            if (newQuery.length > 2) {
                                viewModel.searchCallLogs(newQuery)
                            } else if (newQuery.isEmpty()) {
                                viewModel.searchCallLogs(newQuery)
                            }
                        } else {
                            viewModel.searchContactsQuery.value = newQuery
                            if (newQuery.length > 2) {
                                viewModel.searchContacts(newQuery)
                            } else if (newQuery.isEmpty()) {
                                viewModel.searchContacts(newQuery)
                            }
                        }
                    },
                    onSearch = {},
                    onClear = {
                        if (pagerState.currentPage == 1) {
                            viewModel.query.value = ""
                            viewModel.searchCallLogs("")
                        } else {
                            viewModel.searchContactsQuery.value = ""
                            viewModel.searchContacts("")
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5f)
                        .padding(start = 10.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                        .weight(0.2f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        expanded = true
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_filter),
                            contentDescription = null,
                            tint = Color(0xFF454C55),
                            modifier = Modifier
                                .size(35.dp)
                                .graphicsLayer(scaleX = -1f)
                        )
                    }
                    val context = LocalContext.current
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(clippingEnabled = false),
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(NavyElevated)
                    ) {
                        // ── Navigate ──
                        MenuSectionHeader("Navigate")
                        DropdownMenuItem(
                            onClick = { expanded = false; viewModel.navigateToAnalytics() },
                            leadingIcon = { Icon(Icons.Default.BarChart, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Call Analytics", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; viewModel.navigateToTeam() },
                            leadingIcon = { Icon(Icons.Default.Group, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Team Members", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; viewModel.navigateToAdminDashboard() },
                            leadingIcon = { Icon(Icons.Default.Security, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Admin Dashboard", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; showSaveInappDialog = true },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Create New Contact", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )

                        // ── Export ──
                        Divider(thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.2f))
                        MenuSectionHeader("Export")
                        DropdownMenuItem(
                            onClick = { expanded = false; showDateRangePicker = true; viewModel.exportType.intValue = 1 },
                            leadingIcon = { Icon(Icons.Default.IosShare, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Logs → Excel (.xlsx)", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; showDateRangePicker = true; viewModel.exportType.intValue = 0 },
                            leadingIcon = { Icon(Icons.Default.IosShare, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Logs → PDF", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; viewModel.exportContactsIntoPdf() },
                            leadingIcon = { Icon(Icons.Default.IosShare, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Contacts → PDF", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; viewModel.exportContactsIntoVcf() },
                            leadingIcon = { Icon(Icons.Default.IosShare, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Contacts → VCF", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )

                        // ── Data ──
                        Divider(thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.2f))
                        MenuSectionHeader("Data")
                        DropdownMenuItem(
                            onClick = { expanded = false; backupLauncher.launch("quicklink_caller_backup.db") },
                            leadingIcon = { Icon(Icons.Default.Backup, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Backup Data", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DropdownMenuItem(
                            onClick = { expanded = false; restoreLauncher.launch(arrayOf("*/*")) },
                            leadingIcon = { Icon(Icons.Default.Restore, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Restore Data", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )

                        // ── App ──
                        Divider(thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.2f))
                        MenuSectionHeader("App")
                        DropdownMenuItem(
                            onClick = { expanded = false; EventEmitter.postEvent(Event.HomeVm(2, null)) },
                            leadingIcon = { Icon(Icons.Default.Settings, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Settings", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        DefaultDialerMenuItem(
                            onClick = { expanded = false; viewModel.navigateToDefaultDialer() },
                            onDismissRequest = { expanded = false }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                context.startActivity(Intent(context, WebViewActivity::class.java).apply {
                                    putExtra("url", Constant.Urls.privacyPolicy)
                                    putExtra("type", "Privacy Policy")
                                })
                            },
                            leadingIcon = { Icon(Icons.Default.Security, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            text = { Text("Privacy Policy", fontFamily = montserrat_semibold, color = TextPrimary) }
                        )
                        Divider(thickness = 0.5.dp, color = TextSecondary.copy(alpha = 0.2f))
                        TriStateToggle(viewModel)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            DailyBriefingBanner(viewModel)
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                contentColor = TextPrimary,
                containerColor = NavySurface,
                divider = {
                    Divider(thickness = 0.dp, color = Color.Transparent)
                },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = PurpleSolid,
                    )
                }, modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        modifier = Modifier.fillMaxWidth(),
                        text = {
                            Text(
                                tab.title,
                                color = if (pagerState.currentPage == index) PurpleSolid else TextSecondary,
                                style = normalGoogleSansStyle.copy(fontFamily = google_sans_medium),
                                textAlign = if (index == 0) TextAlign.Start else if (index == 1) TextAlign.Center else TextAlign.End,
                                fontSize = 15.sp.nonScaledSp,
                                modifier = Modifier
                                    .fillMaxWidth(1F)
                                    .padding(horizontal = 5.dp)
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }
            com.google.accompanist.pager.HorizontalPager(
                state = pagerState,
                count = tabs.size
            ) { page ->
                tabs[page].screen()
            }

            /*
                    TabControl(
                        selectedColor = Color(0xFFF4E6E7),
                        unselectedColor = Color(0xFFCECCCC),
                        firstTabContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4E6E7)),
                                contentAlignment = Alignment.TopStart
                            ) {
                                CallLogScreen(viewModel)
                            }
                        }, lastTabContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4E6E7)),
                                contentAlignment = Alignment.TopStart
                            ) {
                                ShowContactsUi(viewModel)
                            }
                        })
            */
            if (circularLoadingIndicator) CircularLoadingIndicator()
        }

    }

}

@Composable
private fun DailyBriefingBanner(viewModel: HomeVm) {
    if (!viewModel.hasGeminiKey) return

    val briefingState by viewModel.briefingState.collectAsState()

    LaunchedEffect(Unit) {
        if (briefingState is HomeVm.BriefingState.Idle) {
            viewModel.loadDailyBriefing()
        }
    }

    when (val state = briefingState) {
        is HomeVm.BriefingState.Loading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyElevated)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = PurpleSolid
                )
                Text("Loading AI briefing…", fontSize = 12.sp, color = TextSecondary)
            }
        }
        is HomeVm.BriefingState.Success -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PurpleSolid.copy(alpha = 0.10f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PurpleSolid,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Text(
                    state.text,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    lineHeight = 18.sp
                )
            }
        }
        else -> {}
    }
}
