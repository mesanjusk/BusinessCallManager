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
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.ruchitech.quicklinkcaller.navhost.nav.NavigationComponent
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.QuicklinkCallerTheme
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
                val statusBarColor = Color.White // Blue

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor,
                        darkIcons = true // false = white icons
                    )
                }


                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.White
                ) {

                    ModalNavigationDrawer(
                        modifier = Modifier.fillMaxWidth(),
                        gesturesEnabled = gesturesEnabled.value,
                        drawerState = drawerState,
                        drawerContent = {}) {

                        Scaffold(
                            topBar = {

                            },
                            bottomBar = {},
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
