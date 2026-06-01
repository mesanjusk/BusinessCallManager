package com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.net.Uri
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.UnitValue
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.formatDate
import com.ruchitech.quicklinkcaller.helper.formatDateFromMillis
import com.ruchitech.quicklinkcaller.helper.formatMilliSecondsToDateTime
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.parseCountryCodes
import com.ruchitech.quicklinkcaller.helper.removeCountryCode
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.helper.syncDeletedSecondaryContacts
import com.ruchitech.quicklinkcaller.helper.syncSecondaryContacts
import com.ruchitech.quicklinkcaller.helper.syncUpdateCallLogs
import com.ruchitech.quicklinkcaller.helper.syncUpdateSecondaryContacts
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.navhost.routes.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.navhost.routes.DefaultDialerRoute
import com.ruchitech.quicklinkcaller.navhost.routes.HomeRoute
import com.ruchitech.quicklinkcaller.navhost.routes.NoteAndReminderRoute
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.navhost.routes.SplashRoute
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.recievers.AlarmReceiver
import com.ruchitech.quicklinkcaller.retrofit.remote.Status
import com.ruchitech.quicklinkcaller.retrofit.repository.AccountRepository
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ai.GeminiService
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.room.data.Tasks
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.home.data.DeleteSecondaryContact
import com.ruchitech.quicklinkcaller.ui.screens.home.data.FetchCallLogs
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val pref: AppPreference,
    private val resourcesProvider: com.ruchitech.quicklinkcaller.data.ResourcesProvider,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference,
    private val accountRepository: AccountRepository,
    private val callLogHelper: CallLogHelper,
    val contactHelper: ContactHelper,
    private val geminiService: GeminiService,
) : SharedViewModel(), RouteNavigator by routeNavigator {
    val tabNumber = mutableIntStateOf(0)

    sealed class BriefingState {
        object Idle : BriefingState()
        object Loading : BriefingState()
        data class Success(val text: String) : BriefingState()
        data class Error(val message: String) : BriefingState()
    }
    private val _briefingState = MutableStateFlow<BriefingState>(BriefingState.Idle)
    val briefingState: StateFlow<BriefingState> = _briefingState.asStateFlow()
    val hasGeminiKey: Boolean get() = !appPreference.geminiApiKey.isNullOrBlank()

    fun loadDailyBriefing() {
        val apiKey = appPreference.geminiApiKey ?: return
        viewModelScope.launch {
            _briefingState.value = BriefingState.Loading
            val today = run {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0); cal.timeInMillis
            }
            val callsToday = try {
                dbRepository.callLogDao.getCallLogsBetween(today, System.currentTimeMillis()).size
            } catch (e: Exception) { 0 }
            val totalLeads = try {
                dbRepository.leadDao.getLeadCount(appPreference.userId ?: "")
            } catch (e: Exception) { 0 }
            val newLeads = try {
                dbRepository.leadDao.getLeadsBySourceBetween("call", today, System.currentTimeMillis()).size +
                    dbRepository.leadDao.getLeadsBySourceBetween("whatsapp", today, System.currentTimeMillis()).size
            } catch (e: Exception) { 0 }
            val result = geminiService.getDailyBriefing(apiKey, totalLeads, newLeads, callsToday, 0)
            _briefingState.value = result.fold(
                onSuccess = { BriefingState.Success(it) },
                onFailure = { BriefingState.Error(it.message ?: "Error") }
            )
        }
    }

    private val _callLogs = MutableStateFlow<List<CallLogsWithDetails>>(emptyList())
    val callLogsData: StateFlow<List<CallLogsWithDetails>> = _callLogs.asStateFlow()

    private val _searchCallLogs = MutableStateFlow<List<CallLogsWithDetails>>(emptyList())
    val searchCallLogs: StateFlow<List<CallLogsWithDetails>> = _searchCallLogs.asStateFlow()
    val callLogsUi = callLogsData
        .map { logs -> logs.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val searchCallLogsUi = searchCallLogs
        .map { logs -> logs.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var searchContactsQuery = mutableStateOf("")

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _searchContacts = MutableStateFlow<List<Contact>>(emptyList())
    val searchContacts: StateFlow<List<Contact>> = _searchContacts.asStateFlow()

    private val _reminder = MutableStateFlow<Reminders?>(null)
    val reminder: StateFlow<Reminders?> = _reminder.asStateFlow()

    private val _callLogForReminder = MutableStateFlow<CallLogDetails?>(null)
    val callLogForReminder: StateFlow<CallLogDetails?> = _callLogForReminder.asStateFlow()

    val showReminderUi = mutableStateOf(false)
    val query = mutableStateOf("")
    val lastClickedCallLog = mutableStateOf<CallLogsWithDetails?>(null)

    //  private val contactHelper by lazy { ContactHelper(resourcesProvider.appContext) }
    val appPreferences = AppPreferences(resourcesProvider.getContext().applicationContext)

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val hasMoreData2 = mutableStateOf(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val currentPage = mutableIntStateOf(0)
    private val currentPageForContacts = mutableIntStateOf(0)
    val exportType = mutableIntStateOf(0) // 0 pdf , 1 excel
    val isNoteFieldOpen = mutableStateOf(false)
    val lastSyncTime = mutableLongStateOf(0L)
    private val pageSize = 150
    private var hasMoreData = true
    private var hasMoreDataForContacts = true
    private var lastLoadTimestamp: Long = 0L
    private var lastLoadTimestampContact: Long = 0L
    private val pageSizeForContacts = 100
    private val minimumTimeDifference: Long =
        3000L  // Set your desired minimum time difference in milliseconds
    var indexNumForEditContact = mutableIntStateOf(0)
    private val debounceInterval = 2000L // 1 second
    private var lastEventTime: Long = 0
    private var dateTimeString = ""

    private val _tasks = MutableStateFlow<List<Tasks?>?>(null)
    val tasks: StateFlow<List<Tasks?>?> = _tasks.asStateFlow()

    private val _types = MutableStateFlow<Set<AllCallerIdOptions?>?>(null)
    val callerIdTypes: StateFlow<Set<AllCallerIdOptions?>?> = _types.asStateFlow()
    var callerPopupType = mutableIntStateOf(1)

    data class CallLogUiModel(
        val model: CallLogsWithDetails,
        val latest: CallLogDetails?,
        val displayName: String,
        val formattedTime: String,
        val avatarBg: Color,
        val avatarText: Color
    )

    private fun CallLogsWithDetails.toUiModel(): CallLogUiModel {
        val latest = callLogDetails.firstOrNull()
        val displayName = latest?.cachedName
            ?.takeIf { it.isNotBlank() && it != "Unknown" }
            ?: "Unknown"
        val formattedTime = latest?.let { formatTimeAgo(it.date) } ?: ""
        val (bg, textColor) = resolveAvatarColors(displayName, latest)
        return CallLogUiModel(
            model = this,
            latest = latest,
            displayName = displayName,
            formattedTime = formattedTime,
            avatarBg = bg,
            avatarText = textColor
        )
    }

    private fun resolveAvatarColors(
        displayName: String,
        latest: CallLogDetails?
    ): Pair<Color, Color> {
        // Secondary contacts stored in-app: keep the legacy orange accent.
        if (latest?.colorCode == Orange) return Orange to Color.Black

        // Saved contacts (phonebook) stay consistent with black chip + light text.
        if (displayName != "Unknown") return Color.Black to Color.White

        // Unknown numbers alternate deterministically between orange and black.
        val seed = latest?.number
            ?.ifBlank { latest.cachedName ?: "" }
            ?: latest?.callerId
            ?: displayName
        val useOrange = (seed.hashCode() and 1) == 0
        val bg = if (useOrange) Orange else Color.Black
        val text = if (bg == Color.Black) Color.White else Color.Black
        return bg to text
    }

    fun signout() {
        appPreference.clearData()
        popToRouteAndNavigate(SplashRoute.route, HomeRoute.route, true)
    }

    init {
        pref.isInitialCallLogDone = true
        loadLogs()
        viewModelScope.launch {
            lastSyncTime.longValue =
                dbRepository.timestampDao.getTimestamps()?.lastCallLogsSync ?: 0L
            if (appPreference.shouldForeground) {
                activateService()
            }
            delay(1000)
            fetchContacts()
            //   generateDummyData()
            val data = dbRepository.callerIDOptions.getCallerIdOptions()
            _types.value = data?.callerIdOptions
            callerPopupType.intValue = data?.callerIdType ?: 1
            
            registerCallLogObserver()
        }
    }
    private fun registerCallLogObserver() {
        val observer = object : android.database.ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val currentTime = System.currentTimeMillis()
                // Debounce multi-firing content observer events
                if (currentTime - lastEventTime > 3000) {
                    lastEventTime = currentTime
                    viewModelScope.launch {
                        // Immediately fetch any new calls into local Room DB
                        updateRecentCallLogs()
                        kotlinx.coroutines.delay(1000)
                        // Repopulate the UI flow array
                        reinitCallLogs()
                    }
                }
            }
        }
        
        try {
            resourcesProvider.getContext().contentResolver.registerContentObserver(
                android.provider.CallLog.Calls.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reinitCallLogs() {
        viewModelScope.launch {
            currentPage.intValue = 0
            _callLogs.value = emptyList()
            loadLogs()
        }
    }

    fun updateRecentCallLogs() {
        viewModelScope.launch {
            callLogHelper.insertRecentCallLogs { }
        }
    }

    fun updateTasks(newData: MutableList<Tasks?>?, onSave: Boolean = false) {
        /*        val nonEmptyTasks = arrayListOf<Tasks>()
                if (onSave) {
                    newData?.forEach {
                        if (it!=null && !it.tasks.isNullOrEmpty()){
                            nonEmptyTasks.add(it)
                        }
                    }
                } else {
                    newData?.forEach {
                        if (it != null) {
                            nonEmptyTasks.add(it)
                        }
                    }
                }*/

        _tasks.value = newData
    }


    private fun activateService() {
        val intent = Intent()
        intent.action = McsConstants.ACTION_SEND
        intent.setPackage(resourcesProvider.appContext.packageName)
        resourcesProvider.appContext.sendBroadcast(Intent(McsConstants.ACTION_SEND))
    }

    fun fetchCallLogsByDate(startDate: String, endDate: String) {
        safeApiSyncFetchContactCall(startDate, endDate)
    }

    private fun safeApiSyncFetchContactCall(startDate: String, endDate: String) {
        viewModelScope.launch {
            val body = FetchCallLogs(startDate, endDate, appPreference.userId!!)
            accountRepository.fetchCallLogs(body)
                .distinctUntilChanged().collectLatest { resources ->
                    when (resources.status) {
                        Status.LOADING -> showLoading()
                        Status.ERROR -> {
                            Log.e("jhgffghj", "$resources")
                            delay(2000)
                            hideLoading()
                            Toast.makeText(
                                resourcesProvider.appContext,
                                "No logs found",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        Status.SUCCESS -> {
                            val data = resources.data
                            if (!data?.logs.isNullOrEmpty()) {
                                if (exportType.intValue == 0) {
                                    data?.logs?.let { exportCallLogsIntoPdf(it) }
                                } else {
                                    data?.logs?.let { exportCallLogsToExcel(it) }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun navigateToDefaultDialer() {
        navigateToRoute(DefaultDialerRoute.route)
    }

    fun updateCallLogById(
        updatedCallLog: MutableList<CallLogsWithDetails>,
    ) {
        _callLogs.value = updatedCallLog
    }

    fun loadMoreData() {
        // Increment the page number to load the next set of data
        if (hasMoreData) {
            val currentTimestamp = System.currentTimeMillis()
            // Check if enough time has passed since the last load
            if (currentTimestamp - lastLoadTimestamp >= minimumTimeDifference) {
                _isLoading.value = true
                currentPage.intValue += 1
                lastLoadTimestamp = currentTimestamp
                loadLogs()
            }
        }
    }

    fun loadMoreContacts() {
        // Increment the page number to load the next set of data
        if (hasMoreDataForContacts) {
            val currentTimestamp = System.currentTimeMillis()
            // Check if enough time has passed since the last load
            if (currentTimestamp - lastLoadTimestampContact >= minimumTimeDifference) {
                _isLoading.value = true
                currentPageForContacts.intValue = currentPageForContacts.intValue + 1
                lastLoadTimestampContact = currentTimestamp
                fetchContacts()
            }
        }
    }


    private fun syncCallLogsInBg() {
        /// syncCallLogs()
        Toast.makeText(
            resourcesProvider.appContext,
            "Call logs successfully synced!",
            Toast.LENGTH_SHORT
        ).show()
    }


    fun exportContactsIntoVcf() {
        viewModelScope.launch {
            contactHelper.exportContactsToVCard(contacts.value) {
                Toast.makeText(resourcesProvider.appContext, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun exportContactsIntoPdf() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Contacts Quicklink$timeStamp.pdf"
        viewModelScope.launch {
            Log.e("jnhbygtffygujiko", "exportCallLogsIntoPdf: ${contacts.value}")
            // Get the download directory
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadDir, fileName)

            // Create a PDF file
            val writer = PdfWriter(pdfFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Add a title to the document
            document.add(Paragraph("Contacts"))

            // Create a table to display call log details
            val table = Table(4).apply {
                width = UnitValue.createPercentValue(100f)
                setMarginTop(20f)
                addHeaderCell("Sr no.")
                addHeaderCell("Name")
                addHeaderCell("Number")
                addHeaderCell("Created Date")
            }

            // Add data rows to the table
            contacts.value.forEachIndexed() { index, callLog ->
                table.addCell(index.plus(1).toString())
                table.addCell(callLog.contact_title)
                table.addCell(callLog.contact_mobile)
                table.addCell(formatDate(callLog.created_at))
            }

            // Add the table to the document
            document.add(table)

            // Close the document
            document.close()

            // Notify the MediaScanner about the new file
            resourcesProvider.appContext.applicationContext.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    android.net.Uri.fromFile(pdfFile)
                )
            )
            delay(1000)
            hideLoading()
            showNotificationForFiles(
                resourcesProvider.appContext,
                pdfFile.absolutePath,
                "Contacts exported"
            )
            Toast.makeText(
                resourcesProvider.appContext,
                pdfFile.absolutePath,
                Toast.LENGTH_LONG
            ).show()

        }

    }


    private fun exportCallLogsIntoPdf(callLogs: List<CallLogDetails>) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Call_Log_Details_$timeStamp.pdf"
        viewModelScope.launch {
            Log.e("jnhbygtffygujiko", "exportCallLogsIntoPdf: $callLogs")
            // Get the download directory
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadDir, fileName)

            // Create a PDF file
            val writer = PdfWriter(pdfFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Add a title to the document
            document.add(Paragraph("Call Log Details"))

            // Create a table to display call log details
            val table = Table(6).apply {
                width = UnitValue.createPercentValue(100f)
                setMarginTop(20f)
                //   addHeaderCell("ID")
                addHeaderCell("Name")
                addHeaderCell("Number")
                addHeaderCell("Type")
                addHeaderCell("Date")
                addHeaderCell("Duration")
                addHeaderCell("Call Note")
            }

            // Add data rows to the table
            callLogs.forEach { callLog ->
                val name = if (callLog.cachedName.isNullOrEmpty()) "Unknown" else callLog.cachedName
                // table.addCell(callLog.id.toString())
                table.addCell(name)
                table.addCell(callLog.number)
                table.addCell(callLog.type.toString())
                table.addCell(formatMilliSecondsToDateTime(callLog.date))
                table.addCell(callLog.duration.toString())
                table.addCell(callLog.callNote ?: "")
            }

            // Add the table to the document
            document.add(table)

            // Close the document
            document.close()

            // Notify the MediaScanner about the new file
            resourcesProvider.appContext.applicationContext.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    android.net.Uri.fromFile(pdfFile)
                )
            )
            delay(1000)
            hideLoading()
            showNotificationForFiles(resourcesProvider.appContext, pdfFile.absolutePath)
            Toast.makeText(
                resourcesProvider.appContext,
                pdfFile.absolutePath,
                Toast.LENGTH_LONG
            ).show()

        }

    }

    private fun exportCallLogsToExcel(callLogs: List<CallLogDetails>) {
        viewModelScope.launch {
            // Create a new workbook
            val workbook = XSSFWorkbook()
            // Create a sheet in the workbook
            val sheet = workbook.createSheet("Call Logs")
            // Create a header row
            val headerRow: Row = sheet.createRow(0)
            val headers = arrayOf(
                "Name",
                "Number",
                "Type",
                "Date",
                "Duration",
                "Call Note",
            )
            for (i in headers.indices) {
                val cell: Cell = headerRow.createCell(i)
                cell.setCellValue(headers[i])
            }

            // Create a date format for formatting date values
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // Create data rows for each call log
            var rowIndex = 1
            for (callLog in callLogs) {
                val row: Row = sheet.createRow(rowIndex++)
                //   row.createCell(0).setCellValue(callLog.id.toDouble())
                //   row.createCell(1).setCellValue(callLog.callerId)
                row.createCell(0).setCellValue(callLog.cachedName ?: "")
                row.createCell(1).setCellValue(callLog.number)
                row.createCell(2).setCellValue(callLog.type.toString())
                row.createCell(3).setCellValue(dateFormat.format(Date(callLog.date)))
                row.createCell(4).setCellValue(callLog.duration.toDouble())
                row.createCell(5).setCellValue(callLog.callNote ?: "")
                // row.createCell(8).setCellValue(if (callLog.isSynced) "Yes" else "No")
            }
            val maxChars = callLogs.map { it.cachedName?.length ?: 0 }.maxOrNull() ?: 0
            val width = (maxChars * 0.55 * 200).toInt()
            // Resize columns to fit content
            for (i in headers.indices) {
                // sheet.autoSizeColumn(i)
                sheet.setColumnWidth(i, width)
            }

            // Get the path to the downloads directory
            val downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // Create a File object for the output file
            val outputFile = File(downloadsDirectory, "Call_Log_Details_${Date().time}.xls")
            // Write the workbook content to the output file
            FileOutputStream(outputFile).use { fileOut ->
                workbook.write(fileOut)
            }
            // Close the workbook to release resources
            workbook.close()
            delay(1000)
            hideLoading()
            showNotificationForFiles(resourcesProvider.appContext, outputFile.absolutePath)
            Toast.makeText(
                resourcesProvider.appContext,
                outputFile.absolutePath,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun updateSettings(type: Int) {
        val selectedOptions = callerIdTypes.value as Set<AllCallerIdOptions>
        //appPreference.callerIdOptions = appPreferences.setCallerIdOptions(selectedOptions)
        viewModelScope.launch {
            callerPopupType.intValue = type
            dbRepository.callerIDOptions.insertOrUpdateCallerIdOptions(
                CallerIdOptionsEntity(
                    callerIdOptions = selectedOptions,
                    callerIdType = type
                )
            )
        }
        Toast.makeText(resourcesProvider.getContext(), "Settings saved!", Toast.LENGTH_SHORT).show()
    }

    fun navigateToNotesAndReminder() {
        navigateToRoute(NoteAndReminderRoute.withArgs("na", "na", "na"))
    }

    fun navigateToAnalytics() {
        navigateToRoute(Screen.AnalyticsRoute.route)
    }

    fun navigateToTeam() {
        navigateToRoute(Screen.TeamManagementScreen.route)
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove non-numeric characters and leading '0' or country code
        var normalizedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (normalizedNumber.startsWith("+91")) {
            // Remove country code if it exists
            normalizedNumber = normalizedNumber.substring(3)
        } else if (normalizedNumber.startsWith("0")) {
            // Remove leading '0'
            normalizedNumber = normalizedNumber.substring(1)
        }
        // Remove any '-' or ' ' characters
        normalizedNumber = normalizedNumber.replace(Regex("[-\\s]"), "")
        return normalizedNumber
    }

    private fun loadLogs() {
        viewModelScope.launch {
            val callLogsList = dbRepository.callLogDao.getPaginatedCallLogsTest(
                pageSize,
                currentPage.intValue * pageSize
            )

            if (!callLogsList.isNullOrEmpty()) {
                try {
                    val mergeLogs = hashMapOf<String, MutableList<CallLogDetails>>()
                    callLogsList.forEachIndexed { _, callLogsWithDetails ->
                        val normalizePhone =
                            callLogsWithDetails.callLogs.callerId // normalizePhoneNumber(callLogsWithDetails.callLogs.callerId)
                        if (mergeLogs.containsKey(normalizePhone)) {
                            mergeLogs[normalizePhone]?.addAll(callLogsWithDetails.callLogDetails)
                        } else {
                            mergeLogs[normalizePhone] =
                                callLogsWithDetails.callLogDetails.toMutableList()
                        }
                    }

                    callLogsList.forEach { check ->
                        val normalizePhone =
                            check.callLogs.callerId //normalizePhoneNumber(check.callLogs.callerId)
                        val tempOrgId = check.callLogs.callerId
                        if (mergeLogs.containsKey(normalizePhone)) {
                            check.callLogs.callerId = normalizePhone
                            check.callLogDetails =
                                mergeLogs[normalizePhone]?.sortedByDescending { it.date }
                                    ?.toMutableList() ?: check.callLogDetails
                        }

                        val contactName =
                            dbRepository.contact.getContactByPhoneNumber(tempOrgId)?.contact_title
                        if (!contactName.isNullOrEmpty()) {
                            check.callLogDetails.forEach { log ->
                                log.cachedName = contactName
                                log.colorCode = Orange
                            }
                        } else if (check.callLogDetails.maxByOrNull { it.date }?.cachedName.isNullOrEmpty() ||
                            check.callLogDetails.maxByOrNull { it.date }?.cachedName == "Unknown"
                        ) {
                            val tempLog = check.callLogDetails.maxByOrNull { it.date }
                            if (tempLog != null) {
                                tempLog.colorCode = Orange
                                tempLog.cachedName = "Unknown"
                            }
                        }
                    }


                    val sortedCallLogs = callLogsList.map { callLogsWithDetails ->
                        callLogsWithDetails.copy(
                            callLogDetails = callLogsWithDetails.callLogDetails.sortedByDescending { it.date }
                                .toMutableList()
                        )
                    }.toMutableList()



                    if (callLogsList.size < pageSize) {
                        hasMoreData = false
                        if (callLogsData.value.size > 50) {
                            showSnackbar("No more call logs available")
                        }
                    }
                    if (currentPage.intValue != 0) {
                        delay(1000)
                    }
                    val newData = sortedCallLogs.sortedByDescending {
                        it.callLogDetails.firstOrNull()?.date
                    }
                    val uniqueNewData = newData.distinctBy { it.callLogs.callerId }
                    if (uniqueNewData.isNotEmpty()) {
                        _callLogs.value = _callLogs.value.plus(uniqueNewData)
                    } else {
                        _callLogs.value = _callLogs.value.plus(sortedCallLogs)
                    }
                } catch (_: Exception) {
                } finally {
                    _isLoading.value = false
                }

            }
        }
    }

    fun searchCallLogs(query: String) {
        viewModelScope.launch {
            val data = dbRepository.callLogDao.searchCallLogs(query)
            if (data.isNotEmpty() && query.isNotEmpty() || query.isNotBlank()) {
                val mergeLogs = hashMapOf<String, MutableList<CallLogDetails>>()
                data.forEachIndexed { _, callLogsWithDetails ->
                    val normalizePhone = callLogsWithDetails.callLogs.callerId
                    //normalizePhoneNumber(callLogsWithDetails.callLogs.callerId)
                    if (mergeLogs.containsKey(normalizePhone)) {
                        mergeLogs[normalizePhone]?.addAll(callLogsWithDetails.callLogDetails)
                    } else {
                        mergeLogs[normalizePhone] =
                            callLogsWithDetails.callLogDetails.toMutableList()
                    }
                }
                val sortedCallLogs = data.map { callLogsWithDetails ->
                    callLogsWithDetails.copy(
                        callLogDetails = callLogsWithDetails.callLogDetails.sortedByDescending { it.date }
                            .toMutableList()
                    )
                }

                sortedCallLogs.forEach { check ->
                    val normalizePhone = check.callLogs.callerId
                    if (mergeLogs.containsKey(normalizePhone)) {
                        check.callLogs.callerId = normalizePhone
                        check.callLogDetails = mergeLogs[normalizePhone]?.toMutableList()!!
                    }

                    val contactName =
                        dbRepository.contact.getContactByPhoneNumber(check.callLogs.callerId)?.contact_title
                    if (!contactName.isNullOrEmpty()) {
                        check.callLogDetails.forEach { log ->
                            log.cachedName = contactName
                            log.colorCode = Orange
                        }
                    } else if (check.callLogDetails.maxByOrNull { it.date }?.cachedName.isNullOrEmpty() ||
                        check.callLogDetails.maxByOrNull { it.date }?.cachedName == "Unknown"
                    ) {
                        val tempLog = check.callLogDetails.maxByOrNull { it.date }
                        if (tempLog != null) {
                            tempLog.colorCode = Orange
                            tempLog.cachedName = "Unknown"
                        }
                    }
                }
                val uniqueNewData = sortedCallLogs.distinctBy { it.callLogs.callerId }
                _searchCallLogs.value = uniqueNewData
            } else {
                _searchCallLogs.value = emptyList()
            }
        }
    }

    fun updateSearchEditLog(newData: List<CallLogsWithDetails>) {
        _searchCallLogs.value = newData
    }


    fun searchContacts(query: String) {
        viewModelScope.launch {
            val searchedData = dbRepository.contact.searchContactsByNameOrNumberSortedByName(query)
            Log.e("kjiuhygtf", "searchContacts: $searchedData")
            if (searchedData.isNotEmpty() && query.isNotEmpty()) {
                _searchContacts.value = searchedData
            } else {
                _searchContacts.value = emptyList()
                resetContacts()
            }
        }
    }


    private fun fetchContacts() {
        viewModelScope.launch {
            // val nextPage = currentPageForContacts.value + pageSizeForContacts
            val number = currentPageForContacts.intValue * pageSizeForContacts
            val data = dbRepository.contact.getContactsPagedSortedByNameTest(
                pageSizeForContacts,
                number
            )

            if (data.isNotEmpty()) {
                if (data.size < pageSizeForContacts) {
                    hasMoreDataForContacts = false
                    if (currentPageForContacts.intValue != 0) showSnackbar("No more contacts available")
                }
                if (currentPageForContacts.intValue != 0) {
                    delay(2000)
                }
                _contacts.value = _contacts.value.plus(data)
            }

        }
    }


    private fun resetContacts() {
        currentPageForContacts.intValue = 0
        _contacts.value = emptyList()
        fetchContacts()
    }

    fun deleteContact(contact: Contact, usingSearch: Boolean = false) {
        viewModelScope.launch {
            dbRepository.contact.deleteContact(contact)
            // Insert the deleted contact into the secondary table
            val deletedContact = DeleteSecondaryContact(contact.contact_uuid, contact.user_uuid)
            dbRepository.contact.insertDeletedContact(deletedContact)
            if (usingSearch) {
                val temp2 = searchContacts.value.toMutableList().minus(contact)
                //val sortedList = temp2.sortedBy { it.contact_title }
                _searchContacts.value = temp2
            }
            val tempList = contacts.value.toMutableList().minus(contact)
            val sortedList = tempList.sortedBy { it.contact_title }
            updateContactList(tempList.toMutableList())
            resourcesProvider.appContext.syncDeletedSecondaryContacts()
        }
    }


    fun insertNoteOnCallLogChild(newNote: String, value: Long) {
        viewModelScope.launch {
            val dataToUpdate = dbRepository.callLogDao.getCallLogById(value)
            val tempData = dataToUpdate?.copy(callNote = newNote, isSynced = false)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
            }
            syncUpdateCallLogs()
        }
    }

    fun insertTasksOnChildLogs(value: Long) {
        viewModelScope.launch {
            val temp = arrayListOf<Tasks>()
            tasks.value?.forEach {
                if (it != null && !it.tasks.isNullOrEmpty()) {
                    temp.add(it)
                }
            }
            val dataToUpdate = dbRepository.callLogDao.getCallLogById(value)
            val tempData =
                dataToUpdate?.copy(isSynced = false, tasks = temp)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
            }
            syncUpdateCallLogs()
        }
    }

    private fun updateContactList(contacts: MutableList<Contact>) {
        _contacts.value = contacts
    }

    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
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
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            showSnackbar("Backup successful")
                        }
                    } else {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            showSnackbar("Database not found")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Log.e("gujfhdgfjdhgufd", "backupDatabase: ${e.message}")
                        showSnackbar("Failed to backup: ${e.message}")
                    }
                }
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val context = resourcesProvider.getContext()
                    val dbFile = context.getDatabasePath("test_task_db")
                    val walFile = context.getDatabasePath("test_task_db-wal")
                    val shmFile = context.getDatabasePath("test_task_db-shm")

                    dbRepository.databaseDao.close()

                    val resolver = context.contentResolver
                    resolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    if (walFile.exists()) {
                        walFile.delete()
                    }
                    if (shmFile.exists()) {
                        shmFile.delete()
                    }

                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, "Data restored successfully! Restarting app...", Toast.LENGTH_LONG).show()
                        delay(1500)
                        
                        val packageManager = context.packageManager
                        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                        val componentName = intent?.component
                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        showSnackbar("Failed to restore: ${e.message}")
                    }
                }
            }
        }
    }

    private fun saveContactInApp(contact: Contact, onResult: (isSuccess: Boolean) -> Unit) {
        viewModelScope.launch {
            val countryCodes = parseCountryCodes(resourcesProvider.appContext)
            val cleanedPhoneNumber = removeCountryCode(contact.contact_mobile, countryCodes)
            Log.e("kijuhygtfr", "saveContactInApp: $cleanedPhoneNumber")
            val exitsContact = dbRepository.contact.getContactByMobile(cleanedPhoneNumber)
            if (exitsContact != null) {
                Toast.makeText(
                    resourcesProvider.appContext,
                    "Number already saved as ${exitsContact.contact_title}",
                    Toast.LENGTH_SHORT
                ).show()
                onResult(false)
                return@launch
            }

            dbRepository.contact.insertContact(
                contact.copy(
                    user_uuid = appPreference.userId,
                    created_at = formatDateFromMillis(Date().time),
                    contact_mobile = cleanedPhoneNumber
                )
            )
            delay(300)
            val getLastAdded = dbRepository.contact.getContactByPhoneNumber(cleanedPhoneNumber)
            updateCallLogsCachedName(cleanedPhoneNumber, contact.contact_title)
            val tempList = contacts.value.toMutableList().plus(getLastAdded!!)
            val sortedList = tempList.sortedByDescending { it.created_at }
            updateContactList(sortedList.toMutableList())
            onResult(true)
            resourcesProvider.appContext.syncSecondaryContacts()
        }

    }

    private fun editContactInApp(contact: Contact) {
        var getContact: Contact? = if (searchContacts.value.isEmpty()) {
            contacts.value.find { it.contact_uuid == contact.contact_uuid }
        } else {
            searchContacts.value.find { it.contact_uuid == contact.contact_uuid }
        }

        viewModelScope.launch {
            getContact = dbRepository.contact.getContactById(contact.contact_uuid)
            if (getContact != null) {
                val tempContact =
                    getContact?.copy(
                        contact_title = contact.contact_title,
                        contact_mobile = contact.contact_mobile,
                        isSynced = false
                    )
                dbRepository.contact.insertContact(
                    tempContact!!.copy(
                        user_uuid = appPreference.userId,
                        //  created_at = formatDateFromMillis(Date().time)
                    )
                )
                //resourcesProvider.appContext.syncSecondaryContacts()
                syncUpdateSecondaryContacts()
            }

        }
        updateCallLogsCachedName(contact.contact_mobile, contact.contact_title)
        try {
            if (searchContacts.value.isNotEmpty()) {
                val tempList =
                    searchContacts.value.toMutableList()//.set(indexNumForEditContact.intValue,contact)
                val getSearchedContact: Contact? =
                    searchContacts.value.find { it.contact_uuid == contact.contact_uuid }
                indexNumForEditContact.intValue = searchContacts.value.indexOf(getSearchedContact)
                tempList[indexNumForEditContact.intValue] = contact
                val sortedList = tempList.sortedBy { it.contact_title }
                _searchContacts.value = tempList
            } else {
                indexNumForEditContact.intValue = contacts.value.indexOf(getContact)
                val tempList =
                    contacts.value.toMutableList()//.set(indexNumForEditContact.intValue,contact)
                tempList[indexNumForEditContact.intValue] = contact
                val sortedList = tempList.sortedBy { it.contact_title }
                updateContactList(tempList.toMutableList())
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e("kmjnhbgvg", "editContactInApp: $e")
        }
        showSnackbar("Contact Updated")
        // isContactAdded.value = false
    }

    fun checkNumberSavedOrNot(name: String?, number: String?) {
        var contact: Contact? = null
        viewModelScope.launch {
            if (!name.isNullOrEmpty() && !number.isNullOrEmpty()) {
                contact = dbRepository.contact.getContactByPhoneNumber(number)
                if (contact != null) {
                    indexNumForEditContact.intValue = contacts.value.indexOf(contact)
                    editContactInApp(contact!!.copy(contact_title = name, contact_mobile = number))
                } else {
                    val tempContact = Contact(
                        contact_title = name,
                        contact_mobile = number,
                        email = "",
                        address = "",
                        created_at = formatDateFromMillis(Date().time)
                    )
                    saveContactInApp(tempContact) {
                        if (it) {

                        }
                    }
                }
            }
        }
    }

    fun openWhatsAppByNum(number: String) {
        resourcesProvider.appContext.openWhatsapp(number)
    }

    fun makeCallToNum(number: String) {
        resourcesProvider.appContext.makePhoneCall(number)
    }

    fun readyForEdit(contact: Contact?) {
        if (contact != null) {
            editContactInApp(contact)
        }
    }

    private fun updateCallLogsCachedName(callerId: String, name: String) {
        viewModelScope.launch {
            // dbRepository.callLogDao.updateCachedNameByCallerId(callerId, named)
            _callLogs.value = _callLogs.value.map { callLog ->
                if (callLog.callLogs.callerId == callerId) {
                    callLog.callLogDetails.map { item ->
                        item.cachedName = name
                    }
                    callLog
                } else {
                    callLog
                }
            }
        }
    }

    private fun refreshCallLog(updatedCallLogsWithDetails: CallLogDetails?) {
        Log.e("kjiuhygf", "refreshCallLog: $updatedCallLogsWithDetails")
        val getIndexOf = callLogsData.value.indexOf(lastClickedCallLog.value)
        val tempList = callLogsData.value.toMutableList()
        val tempData = lastClickedCallLog.value?.callLogDetails?.map { item ->
            if (updatedCallLogsWithDetails?.id == item.id) {
                item.callNote = updatedCallLogsWithDetails.callNote
                item.tasks = updatedCallLogsWithDetails.tasks
            }
        }
        tempList[getIndexOf] = lastClickedCallLog.value!!
        _callLogs.value = tempList
    }


    override fun handleInternalEvent(event: Event) {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastEventTime
        if (timeDifference >= debounceInterval) {
            when (event) {
                is Event.HomeVm -> {
                    if (event.type == 1 && event.any is Contact) {
                        saveContactInApp(event.any) {
                            if (it) {
                                showSnackbar("Contact Saved")
                            }
                        }
                    } else if (event.type == 3 && event.any is Contact) {
                        showSnackbar("Contact Updated")
                        editContactInApp(event.any)
                    } else if (event.type == 2) {
                        navigateToRoute(Screen.SettingsRoute.route)
                    } else if (event.type == 4 && event.any is CallLogDetails) {
                        refreshCallLog(event.any)
                    } else if (event.type == 5) {
                        popToRouteAndNavigate(PrepairDataRoute.route, Screen.HomeScreen.route, true)
                    }
                }

                else -> Unit
            }
            lastEventTime = currentTime
        }
    }

    fun openKeyboardWithoutFocus() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, SHOW_IMPLICIT)
    }

    fun hideKeyboard() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun saveNumberInPhonebook(number: String, name: String) {
        resourcesProvider.appContext.saveNumberToContacts(number, name)
    }

    fun onAddNewAlarm(callLog: CallLogDetails?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val data = dbRepository.callLogDao.getCallLogById(callLog?.id!!)
            _callLogForReminder.value = data
            _reminder.value =
                dbRepository.reminder.getReminderByCallerId(data!!.id)
            onSuccess()
            /*
                        if (!data?.callNote.isNullOrEmpty()) {
                            _callLogForReminder.value = data
                            _reminder.value =
                                dbRepository.reminder.getReminderByCallerId(data!!.id)
                            onSuccess()
                        } else {
                            Toast.makeText(
                                resourcesProvider.appContext,
                                "Please add a note first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
            */

        }
    }

    fun convertTimeStringToMillis(timeString: String): Long {
        val dateFormat = SimpleDateFormat("hh:mm:ss a dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(timeString)
        return date?.time ?: 0
    }

    fun convertMillisTo24HourFormat(inMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = inMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
        val minute = calendar.get(Calendar.MINUTE)

        return String.format("%02d:%02d", hour, minute)
    }

    fun setAlarm(selectedDate: String, date: String, number: String, id: Long?) {
        viewModelScope.launch {
            val inMillis = convertTimeStringToMillis(selectedDate)
            val getHourMin = convertMillisTo24HourFormat(inMillis)
            val data = Reminders(
                id!!,
                getHourMin,
                date,
                callerId = number,
                0,
                true
            )



            dateTimeString = "$getHourMin:00 $date"
            setExactReminder(data)
        }
        Toast.makeText(
            resourcesProvider.appContext,
            "Reminder set successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private suspend fun setExactReminder(data: Reminders) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val exactAlarmPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
            //val useExactAlarmPermission = Manifest.permission.USE_EXACT_ALARM

            if (ContextCompat.checkSelfPermission(
                    resourcesProvider.appContext,
                    exactAlarmPermission
                ) != PackageManager.PERMISSION_GRANTED

            /* ContextCompat.checkSelfPermission(
                 this,
                 useExactAlarmPermission
             ) != PackageManager.PERMISSION_GRANTED*/
            ) {
                Log.e("dliksfsd", "setAlarm: permission not granted")
                scheduleReminder(data)
                /*                // Request the permissions
                                ActivityCompat.requestPermissions(
                                    applicationContext as Activity,
                                    arrayOf(exactAlarmPermission, useExactAlarmPermission),
                                    223
                                )*/

            } else {
                // Permissions already granted, proceed with scheduling the exact alarm
                scheduleReminder(data)
            }
        } else {
            // For versions below Android 12, no need to check runtime permissions
            scheduleReminder(data)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    suspend fun scheduleReminder(data: Reminders) {
        val callerId = callLogForReminder.value?.id
        resourcesProvider.appContext.cancelExistingReminder(callerId.toString())
        val alarmManager =
            resourcesProvider.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTimeString)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        data.timeInMillis = calendar.timeInMillis
        dbRepository.reminder.insertOrUpdateCallerIdOptions(data)

        val intent =
            Intent(
                McsConstants.REMINDER,
                null,
                resourcesProvider.appContext,
                AlarmReceiver::class.java
            )
        intent.putExtra("alarmID", data.reminderId)
        val reminderPendingIntent = PendingIntent.getBroadcast(
            resourcesProvider.appContext,
            (callerId
                ?: 1).toInt(),  // Use callerId as the requestCode to identify the PendingIntent
            intent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        val heartbeatMsFor = McsConstants.ONE_MINUTE
        val i5 = Build.VERSION.SDK_INT
        if (i5 >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                reminderPendingIntent!!
            )
        } else if (i5 < 19) {
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] =
                reminderPendingIntent!!
        } else {
            val i6 = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + i6 * 3,
                i6,
                reminderPendingIntent!!
            )
        }
    }

    fun navigateToCallLogDetails(key1: String, key2: String, key3: CallLogDetails?) {
        navigateToRoute(ChildCallLogRoute.withArgs(key1, key2, key3?.cachedName ?: "Unknown"))
    }

    private fun showNotificationForFiles(
        context: Context,
        filePath: String,
        title: String = "Call logs exported",
    ) {
        val channelId = "file_notification_channel"
        val notificationId = 1001

        // Create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "File Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open the PDF file
        val fileUri =
            FileProvider.getUriForFile(context, context.packageName + ".provider", File(filePath))
        val openIntent = Intent(Intent.ACTION_VIEW)
        openIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        openIntent.setDataAndType(fileUri, "application/pdf")

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText("Tap to open the  file")
            .setSmallIcon(R.drawable.ic_note)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                resourcesProvider.appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


}
