package com.ruchitech.quicklinkcaller

import SaveContactUi
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.formatDateFromMillis
import com.ruchitech.quicklinkcaller.helper.hasAllRequiredPermissions
import com.ruchitech.quicklinkcaller.helper.syncDeletedSecondaryContacts
import com.ruchitech.quicklinkcaller.helper.syncSecondaryContacts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavigationComponent
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.theme.ElectricBlue
import com.ruchitech.quicklinkcaller.ui.theme.NavyElevated
import com.ruchitech.quicklinkcaller.ui.theme.NavyPrimary
import com.ruchitech.quicklinkcaller.ui.theme.NavySurface
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.QuicklinkCallerTheme
import com.ruchitech.quicklinkcaller.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preference: AppPreference
    lateinit var navController: NavHostController

    @Inject
    lateinit var contactHelper: ContactHelper

    var permissionCode = 0 // 1 for call logs
    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            EventEmitter.postEvent(Event.PermissionHandler(permissionCode, isGranted))
            permissionCode = 0
        }

    val requestPermissionLauncher2 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle the result if needed
        }


    override fun onResume() {
        super.onResume()
        if (::navController.isInitialized) {
            if (navController.currentDestination?.route == "home") {
                checkPermissions()
            }
            Log.e("gfoihgiufhgi", "onResume: ${navController.currentDestination?.route}")
        } else {
            // navController is NOT initialized yet
        }

        EventEmitter.postEvent(Event.PermissionHandler(1, false))
        syncSecondaryContacts()
    }

    override fun onDestroy() {
        syncDeletedSecondaryContacts()
        syncSecondaryContacts()
        super.onDestroy()
    }

    fun checkPermissions() {
        val checkPermissions = hasAllRequiredPermissions(this)
        if (!checkPermissions) {
            EventEmitter.postEvent(Event.HomeVm(type = 5))
        }
    }


    @OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Disables edge-to-edge
        /* val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
         val csvFile = File(downloadsDirectory, "Call_logs.csv")

 // Check if file exists
         if (csvFile.exists()) {
             Log.e("juhugfydtrdttfyguyh", "onCreate: 102")
         } else {
             Log.e("juhugfydtrdttfyguyh", "onCreate: 104")
         }*/


        setContent {

            QuicklinkCallerTheme {
                navController = rememberNavController()
                LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val gesturesEnabled = remember {
                    mutableStateOf(false)
                }
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                currentBackStackEntry?.destination
                var showSaveInappDialog by remember {
                    mutableStateOf(false)
                }

                LocalSoftwareKeyboardController.current


                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = NavyPrimary,
                        darkIcons = true
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(), color = NavyPrimary
                ) {

                    ModalNavigationDrawer(
                        modifier = Modifier.fillMaxWidth(),
                        gesturesEnabled = gesturesEnabled.value,
                        drawerState = drawerState,
                        drawerContent = {}) {

                        Scaffold(
                            topBar = {

                            },
                            bottomBar = {
                                val currentRoute = currentBackStackEntry?.destination?.route
                                val bottomNavRoutes = setOf(
                                    Screen.HomeScreen.route,
                                    Screen.LeadListScreen.route,
                                    Screen.TasksScreen.route,
                                    Screen.ActivityScreen.route,
                                    Screen.TeamManagementScreen.route
                                )
                                if (currentRoute in bottomNavRoutes) {
                                    NavigationBar(containerColor = NavySurface) {
                                        val navColors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ElectricBlue,
                                            selectedTextColor = ElectricBlue,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary,
                                            indicatorColor = NavyElevated
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == Screen.HomeScreen.route,
                                            onClick = { navController.navigate(Screen.HomeScreen.route) { launchSingleTop = true } },
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                            label = { Text("Home") },
                                            colors = navColors
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == Screen.LeadListScreen.route,
                                            onClick = { navController.navigate(Screen.LeadListScreen.route) { launchSingleTop = true } },
                                            icon = { Icon(Icons.Default.Group, contentDescription = "Leads") },
                                            label = { Text("Leads") },
                                            colors = navColors
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == Screen.TasksScreen.route,
                                            onClick = { navController.navigate(Screen.TasksScreen.route) { launchSingleTop = true } },
                                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                                            label = { Text("Tasks") },
                                            colors = navColors
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == Screen.ActivityScreen.route,
                                            onClick = { navController.navigate(Screen.ActivityScreen.route) { launchSingleTop = true } },
                                            icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
                                            label = { Text("Activity") },
                                            colors = navColors
                                        )
                                        NavigationBarItem(
                                            selected = currentRoute == Screen.TeamManagementScreen.route,
                                            onClick = { navController.navigate(Screen.TeamManagementScreen.route) { launchSingleTop = true } },
                                            icon = { Icon(Icons.Default.Groups, contentDescription = "Teams") },
                                            label = { Text("Teams") },
                                            colors = navColors
                                        )
                                    }
                                }
                            },
                            /*                            floatingActionButton = {
                                                            Log.e("gfiohguifohgn", "onCreate: ${currentDestination?.route}  ${WindowInsets.isImeVisible}")
                                                            when (currentDestination?.route) {
                                                                Screen.HomeScreen.route -> {
                                                                    if (!WindowInsets.isImeVisible) {
                                                                        MultiFloatingActionButton(
                                                                            fabIcon = painterResource(id = R.drawable.baseline_add_24),
                                                                            items = arrayListOf(
                                                                                FabItem(
                                                                                    icon = painterResource(id = R.drawable.baseline_add_24),
                                                                                    label = "Add New Contact"
                                                                                ) {
                                                                                    showSaveInappDialog = true
                                                                                },
                                                                                */
                            /*                                                       FabItem(
                                                                                       icon = painterResource(id = R.drawable.baseline_settings_24),
                                                                                       label = "Settings"
                                                                                   ) {
                                                                                       EventEmitter.postEvent(Event.HomeVm(2, null))
                                                                                   }*/
                            /*
                                                                            ), onStateChanged = {

                                                                               }
                                                                            )

                                                                    }

                                                                    */
                            /*
                                                                                                                FloatingActionButton(onClick = {
                                                                                                                preference.shouldForeground = true

                                                                                                                Toast.makeText(
                                                                                                                    this,
                                                                                                                    "Service is going to start very soon",
                                                                                                                    Toast.LENGTH_SHORT
                                                                                                                ).show()
                                                                                                            }) {
                                                                                                                Icon(
                                                                                                                    imageVector = Icons.Default.Settings,
                                                                                                                    contentDescription = null
                                                                                                                )
                                                                                                            }
                                                                    */
                            /*
                                                                }
                                                            }
                                                        }*/
                            floatingActionButtonPosition = FabPosition.End,
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState)
                            },
                        ) {
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
                                    }, contactHelper = contactHelper)

                                }
                            }
                            NavigationComponent(navController, snackbarHostState, it)
                        }
                    }
                }


            }
        }
        Log.e("gfiohgiofjgmofg", "onCreate: 243")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            Log.e("gfiohgiofjgmofg", "onCreate: $insets")
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
    }


    @Composable
    fun isKeyboardVisible(): Boolean {
        val density = LocalDensity.current
        val imeBottom = WindowInsets.ime.getBottom(density)
        return imeBottom > 0
    }


}
