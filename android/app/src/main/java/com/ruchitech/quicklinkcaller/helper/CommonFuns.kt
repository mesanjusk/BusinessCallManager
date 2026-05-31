package com.ruchitech.quicklinkcaller.helper

import android.Manifest
import android.R.attr.resource
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.contactutills.CountryCodes
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.room.DatabaseDao
import com.ruchitech.quicklinkcaller.workers.CallLogsSyncWorker
import com.ruchitech.quicklinkcaller.workers.ContactSyncWorker
import com.ruchitech.quicklinkcaller.workers.DeleteContactSyncWorker
import com.ruchitech.quicklinkcaller.workers.ReminderSyncWorker
import com.ruchitech.quicklinkcaller.workers.UpdateCallLogsSyncWorker
import com.ruchitech.quicklinkcaller.workers.AppLogsSyncWorker
import com.ruchitech.quicklinkcaller.workers.UpdateContactSyncWorker
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.json.JSONException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import androidx.core.net.toUri

fun isPhoneNumberValid(phoneNumber: String): Boolean {
    // Check if the string is numeric and has exactly 10 digits
    return phoneNumber.matches(Regex("\\d{10}"))
}


fun generateUniqueContactUUID(anyExtraData: String): String {
    val randomUUID = UUID.randomUUID()
    val currentTimeMillis = System.currentTimeMillis()
    return "$randomUUID-$currentTimeMillis"
}

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = manager.getRunningServices(Integer.MAX_VALUE)

    for (service in services) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }

    return false
}

fun Context.syncSecondaryContacts() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<ContactSyncWorker>()
        .setConstraints(constraints)
        .build()

    val workManager = WorkManager.getInstance()
    workManager.enqueueUniqueWork("SyncContactsWork", ExistingWorkPolicy.REPLACE, syncWorkRequest)
}

fun syncUpdateSecondaryContacts() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateContactSyncWorker>()
        .setConstraints(constraints)
        .build()

    val workManager = WorkManager.getInstance()
    workManager.enqueueUniqueWork(
        "syncUpdateSecondaryContacts",
        ExistingWorkPolicy.REPLACE,
        syncWorkRequest
    )
}

fun syncCallLogs() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<CallLogsSyncWorker>()
        .setConstraints(constraints)
        .build()

    val workManager = WorkManager.getInstance()
    workManager.enqueueUniqueWork("syncCallLogs", ExistingWorkPolicy.REPLACE, syncWorkRequest)
}

fun syncReminders() {
    val constraints = Constraints.Builder()
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<ReminderSyncWorker>()
        .setConstraints(constraints)
        .build()

    val workManager = WorkManager.getInstance()
    workManager.enqueueUniqueWork("syncReminders", ExistingWorkPolicy.REPLACE, syncWorkRequest)
}

fun syncUpdateCallLogs() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<UpdateCallLogsSyncWorker>()
        .setConstraints(constraints)
        .build()

    val workManager = WorkManager.getInstance()
    workManager.enqueueUniqueWork("syncUpdateCallLogs", ExistingWorkPolicy.REPLACE, syncWorkRequest)
}


fun Context.syncDeletedSecondaryContacts() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<DeleteContactSyncWorker>()
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueue(syncWorkRequest)
}


fun checkPermissions(context: Context): Boolean {
    val permissionsToCheck =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            )
        } else {
            listOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            )
        }

    for (permission in permissionsToCheck) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            return false
        }
    }

    // All permissions are granted
    return true
}

fun formatTimeAgo(timeInMillis: Long): String {
    val currentTime = System.currentTimeMillis()
    val difference = currentTime - timeInMillis

    val seconds = difference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "$seconds sec"
        minutes < 60 -> "$minutes min"
        hours < 24 -> "$hours hrs"
        days == 1L -> "yesterday"
        days <= 7 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timeInMillis))
        else -> {
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)

            cal.timeInMillis = timeInMillis
            val year = cal.get(Calendar.YEAR)

            if (year == currentYear) {
                SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(timeInMillis))
            } else {
                SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(timeInMillis))
            }
        }
    }
}



fun formatReminderTime(timeInMillis2: Long): String {
    val currentTime = Calendar.getInstance()
    val reminderTime = Calendar.getInstance().apply { timeInMillis = timeInMillis2 }

    return when {
        reminderTime.timeInMillis < currentTime.timeInMillis -> {
            "" // Return empty string for past reminder
        }

        DateUtils.isToday(reminderTime.timeInMillis) -> {
            "Today " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(reminderTime.time)
        }

        DateUtils.isToday(reminderTime.timeInMillis - DateUtils.DAY_IN_MILLIS) -> {
            "Tomorrow " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(reminderTime.time)
        }

        currentTime[Calendar.WEEK_OF_YEAR] == reminderTime[Calendar.WEEK_OF_YEAR] -> {
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(reminderTime.time)
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(reminderTime.time)
            "$dayOfWeek $time"
        }

        else -> {
            SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(reminderTime.time)
        }
    }
}


/**
 * yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
 */
fun formatDateFromMillis(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val date = Date(millis)
    return sdf.format(date)
}

/**
 * dd-MMM-yyyy
 */
fun formatDate(inputDate: String?): String? {
    // Parse input date string
    return if (!inputDate.isNullOrEmpty()) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = dateFormat.parse(inputDate)
        val outputFormat = SimpleDateFormat("dd-MMM-yy HH:mm", Locale.getDefault())
        outputFormat.format(date)
    } else {
        ""
    }
}

fun formatTimestampToDateTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

fun formatTimestampToDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

fun Context.openWhatsapp(number: String, msg: String = "") {
    try {
        val formattedNumber = if (!number.startsWith("+91")) {
            // If the number doesn't start with +91, add it
            "+91$number"
        } else {
            // If the number already has +91, keep it as is
            number
        }

        val i = Intent(Intent.ACTION_VIEW)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val encodedNumber = URLEncoder.encode(formattedNumber, "UTF-8")
        i.data = "whatsapp://send?phone=$encodedNumber&text=${
            URLEncoder.encode(
                msg,
                "UTF-8"
            )
        }".toUri()

        startActivity(i)
    } catch (e: Exception) {
        Toast.makeText(this, "Whatsapp not installed!", Toast.LENGTH_LONG).show()
    }
}

fun Context.makePhoneCall(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = "tel:$phoneNumber".toUri()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.saveNumberToContacts(phoneNumber: String, name: String? = null) {
    val intent = Intent(Intent.ACTION_INSERT)
    intent.type = ContactsContract.Contacts.CONTENT_TYPE
    intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (name != null) {
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
    }
    startActivity(intent)
}

fun extractInitials(name: String?): String {
    val names = name?.trim()?.split("\\s+".toRegex()) // Split the name into parts
    val initials = StringBuilder()

    for (i in 0 until minOf(
        2,
        names?.size ?: 0
    )) { // Loop through the first two names or all if less than two
        initials.append(names?.get(i)?.get(0) ?: 0).toString()
            .uppercase(Locale.ROOT) // Append the first character of each name
    }

    return initials.ifEmpty { "Un" }.toString().uppercase()
}

fun Context.sendTextMessage(phoneNumber: String, message: String? = null) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phoneNumber")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (message != null) {
            putExtra("sms_body", message)
        }
    }

    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No SMS app found.", Toast.LENGTH_SHORT).show()
    }
}

fun Context.cancelExistingReminder(callerId: String) {
    val existingIntent =
        Intent(McsConstants.REMINDER, null, this, CallStateDetectionService::class.java)
    val existingPendingIntent = PendingIntent.getService(
        this,
        callerId.toInt(),  // Use callerId as the requestCode to identify the PendingIntent
        existingIntent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    existingPendingIntent?.let {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(it)
        it.cancel()
    }
}

fun isTimeInPast(time24Hours: String): Boolean {
    val currentTime = Calendar.getInstance()
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    try {
        val parsedTime = sdf.parse(time24Hours)
        val calendar = Calendar.getInstance().apply {
            time = parsedTime
            set(
                currentTime.get(Calendar.YEAR),
                currentTime.get(Calendar.MONTH),
                currentTime.get(Calendar.DAY_OF_MONTH)
            )
        }

        // Compare with the current time
        return calendar.before(currentTime)
    } catch (e: Exception) {
        // Handle parsing exceptions
        e.printStackTrace()
    }

    // Return false in case of errors
    return false
}

fun isTimeInFuture(timeInMillis: Long): Boolean {
    val currentTimeMillis = System.currentTimeMillis()
    return timeInMillis > currentTimeMillis
}

fun isDateInPast(dateString: String): Boolean {
    val currentDate = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    try {
        val parsedDate = sdf.parse(dateString)
        val calendar = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Compare with the current date
        return calendar.before(currentDate)
    } catch (e: Exception) {
        // Handle parsing exceptions
        e.printStackTrace()
    }

    // Return false in case of errors
    return false
}

fun formatDuration(durationInSeconds: Long): String {
    val hours = durationInSeconds / 3600
    val minutes = (durationInSeconds % 3600) / 60
    val seconds = durationInSeconds % 60

    val formattedDuration = StringBuilder()

    if (hours > 0) {
        formattedDuration.append("${hours}h ")
    }

    if (minutes > 0) {
        formattedDuration.append("${minutes}m ")
    }

    formattedDuration.append("${seconds}s")

    return formattedDuration.toString()
}

fun formatMilliSecondsToDateTime(milliSeconds: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun isWithin31Days(startDateInMillis: Long, endDateInMillis: Long): Boolean {
    // Calculate the difference in milliseconds between the two dates
    val differenceInMillis = abs(startDateInMillis - endDateInMillis)

    // Convert milliseconds to days
    val daysDifference = TimeUnit.MILLISECONDS.toDays(differenceInMillis)

    // Check if the difference is within 31 days
    return daysDifference <= 31
}

fun setStartTimeOfDay(dateInMillis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateInMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun setEndTimeOfDay(dateInMillis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateInMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}


fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun showAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
}

fun checkPermissionsAll(context: Context): List<String> {
    val listOfEnabledPermissions = arrayListOf<String>()
    val permissionsToCheck =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        } else {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
            )
        }

    for (permission in permissionsToCheck) {
        if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
            if (Settings.canDrawOverlays(context)) {
                listOfEnabledPermissions.add(permission)
            } else {
                Log.e("fgdgdfgfdg", "checkPermissions: $permission")
            }
        } else if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            Log.e("fgdgdfgfdg", "checkPermissions: $permission")
            // Permission is not granted
        } else {
            listOfEnabledPermissions.add(permission)
        }
    }

    // All permissions are granted
    return listOfEnabledPermissions
}

fun hasAllRequiredPermissions(context: Context): Boolean {
    val permissionsToCheck =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
            //    Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
              //  Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        } else {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
            //    Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        }

    for (permission in permissionsToCheck) {
        if (permission == Manifest.permission.SYSTEM_ALERT_WINDOW) {
            if (!Settings.canDrawOverlays(context)) {
                Log.e("PermissionCheck", "Missing permission: $permission")
                return false
            }
        } else if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.e("PermissionCheck", "Missing permission: $permission")
            return false // ❌ At least one permission is missing
        }
    }
    val canDrawOverlays = Settings.canDrawOverlays(context)
    return canDrawOverlays // ✅ All granted
}


fun Context.openBatteryOptimizationSettings() {
    val packageName = packageName
    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
    if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}

fun Context.copyToClipboard(textToCopy: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", textToCopy)
    clipboardManager.setPrimaryClip(clip)
    Toast.makeText(this, "Number Copied", Toast.LENGTH_SHORT).show()
}

fun Context.shareContact(name: String, phoneNumber: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Details")
    shareIntent.putExtra(Intent.EXTRA_TEXT, "Phone Number: $phoneNumber\nName: $name")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    // You can specify specific apps to share with by adding their package names to the intent
    // shareIntent.setPackage("com.whatsapp") // Example: Share only with WhatsApp
    startActivity(Intent.createChooser(shareIntent, "Share using").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

fun parseCountryCodes(context: Context): MutableList<CountryCodes.CountryCodesItem> {
    val countryCodes = mutableListOf<CountryCodes.CountryCodesItem>()
    try {
        val inputStream: InputStream = context.assets.open("CountryCodes.json")
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val json = String(buffer, Charsets.UTF_8)
        val data = Gson().fromJson(json, CountryCodes::class.java)
        countryCodes.addAll(data)
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return countryCodes
}

fun removeCountryCode(
    phoneNumber: String,
    countryCodes: List<CountryCodes.CountryCodesItem>,
): String {
    var number = phoneNumber.trim().replace("\\s".toRegex(), "")

    for (countryCode in countryCodes) {
        if (number.startsWith(countryCode.dial_code)) {
            number = number.removePrefix(countryCode.dial_code)
                .trimStart { it.isWhitespace() || it == '+' }
            break
        }
    }

    return number
}

fun exportDatabase(context: Context) {
    val dbFile = context.getDatabasePath("test_task_db")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 and above, use MediaStore API to write to Downloads
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "test_task_db.db")
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri =
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    FileInputStream(dbFile).use { input ->
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (input.read(buffer).also { length = it } > 0) {
                            outputStream?.write(buffer, 0, length)
                        }
                    }
                }
                Log.e("lghdfngdfo", "Database exported successfully to Downloads folder")
            } catch (e: IOException) {
                e.printStackTrace()
                println("Failed to export database")
                Log.e("lghdfngdfo", "Failed to export database")
            }
        }
    } else {
        // For Android 9 and below, write directly to Downloads folder
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "test_task_db.db"
        )

        try {
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupDir).use { output ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                }
            }
            Log.e(
                "lghdfngdfo",
                "exportDatabase: Database exported successfully to: ${backupDir.absolutePath}}"
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("lghdfngdfo", "Failed to export database")
        }
    }
}

suspend fun exportDatabaseAsSQL(context: Context) {
    val db = Room.databaseBuilder(context, DatabaseDao::class.java, "test_task_db").build()

    // StringBuilder to hold the SQL dump
    val sqlDump = StringBuilder()

    // Export the schema (CREATE TABLE statements)
    val tables = listOf("contacts", "Call_log_details") // Add your table names here
    for (table in tables) {
        val createTableQuery = getCreateTableStatement(db, table)
        sqlDump.append(createTableQuery).append(";\n\n")

        // Export the data (INSERT INTO statements)
        val insertStatements = getInsertStatements(db, table)
        sqlDump.append(insertStatements).append("\n\n")
    }

    // Now export this SQL dump to the Downloads folder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 and above, use MediaStore API to write to Downloads
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "database_backup.sql")
            put(MediaStore.Downloads.MIME_TYPE, "application/sql")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri =
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    outputStream?.write(sqlDump.toString().toByteArray())
                }
                println("Database exported successfully to Downloads folder as SQL")
            } catch (e: IOException) {
                e.printStackTrace()
                println("Failed to export database")
            }
        }
    } else {
        // For Android 9 and below, write directly to Downloads folder
        val backupFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "database_backup.sql"
        )

        try {
            FileOutputStream(backupFile).use { outputStream ->
                outputStream.write(sqlDump.toString().toByteArray())
            }
            println("Database exported successfully to: ${backupFile.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            println("Failed to export database")
        }
    }
}

// Function to generate CREATE TABLE statement
fun getCreateTableStatement(db: DatabaseDao, tableName: String): String {
    val query = SimpleSQLiteQuery(
        "SELECT sql FROM sqlite_master WHERE type='table' AND name=?",
        arrayOf(tableName)
    )
    val cursor = db.query(query)
    cursor.use {
        if (it.moveToFirst()) {
            return it.getString(0)
        }
    }
    return ""
}

// Function to generate INSERT INTO statements
fun getInsertStatements(db: DatabaseDao, tableName: String): String {
    val insertStatements = StringBuilder()
    val cursor = db.query(SimpleSQLiteQuery("SELECT * FROM $tableName"))

    if (cursor.count > 0) {
        val columnNames = cursor.columnNames
        while (cursor.moveToNext()) {
            val values = columnNames.joinToString(", ") { column ->
                // Escape values correctly, adding quotes for strings
                when (val value = cursor.getString(cursor.getColumnIndexOrThrow(column))) {
                    null -> "NULL"
                    else -> "'${value.replace("'", "''")}'" // Escape single quotes
                }
            }
            insertStatements.append("INSERT INTO $tableName (${columnNames.joinToString(", ")}) VALUES ($values);\n")
        }
    }
    cursor.close()
    return insertStatements.toString()
}

fun PhoneNumberUtil.normalizePhoneNumber(phoneNumber: String, defaultRegion: String): String? {
    return try {
        val number = parse(phoneNumber, defaultRegion)
        format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
    } catch (e: NumberParseException) {
        null // Invalid number
    }
}

private var lastColorIndex: Int? = null

fun getReadableRandomColor(name: String): Color {
    val colors = listOf(
        Color(0xFF467AD0), // Gray
        Color(0xFF838A94), // Blue
        Color(0xFF938FE4), // Orange
        Color(0xFFB4A699),
        Color(0xFF59B0B9),
        Color(0xFFE5B344)  // Purple
    )

    // Start with a random index based on hash
    var index = abs(name.hashCode() + System.identityHashCode(name)) % colors.size

    // Avoid same as last color
    if (index == lastColorIndex && colors.size > 1) {
        index = (index + 1 + (0..1).random()) % colors.size
    }

    lastColorIndex = index
    return colors[index].copy(alpha = 0.95f)
}


fun isColorDark(color: Color): Boolean {
    val darkness =
        1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return darkness >= 0.5
}

fun syncAppLogs() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val syncWorkRequest = OneTimeWorkRequestBuilder<AppLogsSyncWorker>()
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance().enqueueUniqueWork("syncAppLogs", ExistingWorkPolicy.REPLACE, syncWorkRequest)
}