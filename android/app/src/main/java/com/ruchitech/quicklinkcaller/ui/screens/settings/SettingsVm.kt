package com.ruchitech.quicklinkcaller.ui.screens.settings

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.ai.GoogleAuthHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.isServiceRunning
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.OtpRequestRoute
import com.ruchitech.quicklinkcaller.navhost.routes.SettingsRoute
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class SettingsVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    val appPreference: AppPreference,
    private val resourcesProvider: ResourcesProvider,
    private val dbRepository: DbRepository,
    private val accountRepository: AccountRepository,
    private val googleAuthHelper: GoogleAuthHelper,
    savedStateHandle: SavedStateHandle,
) : SharedViewModel(), RouteNavigator by routeNavigator {

    // Google account AI auth state
    sealed class GoogleAiState {
        object Idle : GoogleAiState()
        object Connecting : GoogleAiState()
        data class Connected(val email: String) : GoogleAiState()
        data class ConsentRequired(val intent: Intent) : GoogleAiState()
        data class Error(val message: String) : GoogleAiState()
    }

    private val _googleAiState = MutableStateFlow<GoogleAiState>(
        if (!appPreference.googleAccountEmail.isNullOrBlank())
            GoogleAiState.Connected(appPreference.googleAccountEmail!!)
        else GoogleAiState.Idle
    )
    val googleAiState: StateFlow<GoogleAiState> = _googleAiState.asStateFlow()

    fun getAccountPickerIntent(): Intent {
        return AccountManager.newChooseAccountIntent(
            null, null,
            arrayOf("com.google"),
            null, null, null, null
        )
    }

    fun connectGoogleAccount(email: String) {
        appPreference.googleAccountEmail = email
        _googleAiState.value = GoogleAiState.Connecting
        viewModelScope.launch {
            when (val result = googleAuthHelper.getToken(email)) {
                is GoogleAuthHelper.TokenResult.Success -> {
                    _googleAiState.value = GoogleAiState.Connected(email)
                    showSnackbar("Google account connected — AI features enabled!")
                }
                is GoogleAuthHelper.TokenResult.ConsentRequired -> {
                    _googleAiState.value = GoogleAiState.ConsentRequired(result.intent)
                }
                is GoogleAuthHelper.TokenResult.Failure -> {
                    appPreference.googleAccountEmail = null
                    _googleAiState.value = GoogleAiState.Error(result.error)
                    showSnackbar("Connection failed: ${result.error}")
                }
            }
        }
    }

    fun handleConsentResult(approved: Boolean) {
        val email = appPreference.googleAccountEmail ?: return
        if (approved) {
            viewModelScope.launch {
                when (val result = googleAuthHelper.getToken(email)) {
                    is GoogleAuthHelper.TokenResult.Success -> {
                        _googleAiState.value = GoogleAiState.Connected(email)
                        showSnackbar("AI features enabled!")
                    }
                    else -> {
                        _googleAiState.value = GoogleAiState.Error("Could not connect")
                    }
                }
            }
        } else {
            appPreference.googleAccountEmail = null
            _googleAiState.value = GoogleAiState.Idle
        }
    }

    fun disconnectGoogleAccount() {
        val email = appPreference.googleAccountEmail ?: return
        viewModelScope.launch { googleAuthHelper.invalidateToken(email) }
        appPreference.googleAccountEmail = null
        _googleAiState.value = GoogleAiState.Idle
        showSnackbar("Google account disconnected")
    }
    val appPreferences = AppPreferences(resourcesProvider.getContext().applicationContext)

    //var types = mutableStateOf<Set<AllCallerIdOptions?>?>(null)
    private val _types = MutableStateFlow<Set<AllCallerIdOptions?>?>(null)
    val callLogsData: StateFlow<Set<AllCallerIdOptions?>?> = _types.asStateFlow()

    init {
        viewModelScope.launch {
            val data = dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions
            _types.value = data
        }
    }

    fun updateCallerIdState(selectedOptions: Set<AllCallerIdOptions?>?) {
        _types.value = selectedOptions
    }

    fun updateSettings(selectedOptions: Set<AllCallerIdOptions?>?, type: Int) {
        appPreference.callerIdOptions = selectedOptions as Set<AllCallerIdOptions>
        appPreferences.setCallerIdOptions(selectedOptions)
        viewModelScope.launch {
            dbRepository.callerIDOptions.insertOrUpdateCallerIdOptions(
                CallerIdOptionsEntity(
                    callerIdOptions = selectedOptions,
                    callerIdType = type
                )
            )
        }
        showSnackbar("Settings saved!")
    }

    fun updateCallerIdType(type: Int) {
        Log.e("lkjihuygtfr", "updateCallerIdType: $type")
        appPreference.callerIdType = type
        showSnackbar("Settings saved!")
    }

    fun enableDisableCallerIdService(isServiceEnabled: Boolean) {
        appPreference.shouldForeground = isServiceEnabled
        if (isServiceEnabled) {
            activateService()
        } else {
            stopAppCallerIdService(resourcesProvider.appContext)
        }
    }

    private fun activateService() {
        val intent = Intent()
        intent.action = McsConstants.ACTION_SEND
        intent.setPackage(resourcesProvider.appContext.packageName)
        resourcesProvider.appContext.sendBroadcast(Intent(McsConstants.ACTION_SEND))
    }

    private fun stopAppCallerIdService(context: Context) {
        if (isServiceRunning(context = context, CallStateDetectionService::class.java)) {
            val serviceIntent = Intent(context, CallStateDetectionService::class.java)
            context.stopService(serviceIntent)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            accountRepository.deleteUser()
                .distinctUntilChanged()
                .collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            delay(5000)
                            hideLoading()
                            showSnackbar(resources.message)
                        }

                        Status.SUCCESS -> {
                            delay(2000)
                            hideLoading()
                            val data = resources.data
                            showSnackbar(resources.data?.message)
                            if (data != null) {
                                if (data.isSuccess)
                                    popToRouteAndNavigate(
                                        OtpRequestRoute.route,
                                        SettingsRoute.route,
                                        true
                                    )
                            }
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun saveGeminiApiKey(key: String) {
        appPreference.geminiApiKey = key.trim().ifBlank { null }
        showSnackbar(if (key.isBlank()) "Gemini API key removed" else "Gemini API key saved")
    }

    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Checkpoint database to merge WAL
                    dbRepository.databaseDao.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                        cursor.moveToFirst()
                    }
                    
                    val dbFile = resourcesProvider.getContext().getDatabasePath("test_task_db")
                    if (dbFile.exists()) {
                        val resolver = resourcesProvider.getContext().contentResolver
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            FileInputStream(dbFile).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            showSnackbar("Backup successful")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showSnackbar("Database not found")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showSnackbar("Failed to backup: ${e.message}")
                    }
                }
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val context = resourcesProvider.getContext()
                    val dbFile = context.getDatabasePath("test_task_db")
                    val walFile = context.getDatabasePath("test_task_db-wal")
                    val shmFile = context.getDatabasePath("test_task_db-shm")

                    // Close the current database connection
                    dbRepository.databaseDao.close()

                    // Replace the db file
                    val resolver = context.contentResolver
                    resolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // Delete WAL and SHM files to avoid corruption when using replaced DB
                    if (walFile.exists()) {
                        walFile.delete()
                    }
                    if (shmFile.exists()) {
                        shmFile.delete()
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Data restored successfully! Restarting app...", Toast.LENGTH_LONG).show()
                        delay(1500)
                        
                        // Restart app after restore
                        val packageManager = context.packageManager
                        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                        val componentName = intent?.component
                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showSnackbar("Failed to restore: ${e.message}")
                    }
                }
            }
        }
    }

}