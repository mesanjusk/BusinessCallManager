package com.ruchitech.quicklinkcaller.helper

import android.util.Log

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Logger(private val tag: String, private val logFile: File) {
    private val maxFileSize = 2 * 1024 * 1024 // 5MB in bytes
    fun logVerbose(message: String) {
        checkFileSize() // Check the file size before writing
        Log.v(tag, message)
        appendLog("VERBOSE: $message")
    }

    fun logDebug(message: String) {
        checkFileSize() // Check the file size before writing
        Log.d(tag, message)
        appendLog("DEBUG: $message")
    }

    fun logInfo(message: String) {
        checkFileSize() // Check the file size before writing
        Log.i(tag, message)
        appendLog("INFO: $message")
    }

    fun logWarning(message: String) {
        checkFileSize() // Check the file size before writing
        Log.w(tag, message)
        appendLog("WARNING: $message")
    }

    fun logError(message: String, throwable: Throwable? = null) {
        checkFileSize() // Check the file size before writing
        if (throwable != null) {
            Log.e(tag, message, throwable)
            appendLog("ERROR: $message\n${Log.getStackTraceString(throwable)}")
        } else {
            Log.e(tag, message)
            appendLog("ERROR: $message")
        }
    }

    private fun appendLog(logMessage: String) {
        try {
            val timestamp = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            FileWriter(logFile, true).use { writer ->
                writer.append("$timestamp - $logMessage\n")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error writing to log file", e)
        }
    }

    private fun checkFileSize() {
        if (logFile.exists() && logFile.length() >= maxFileSize) {
            Log.i(tag, "temp file deleted successfully")
            logFile.delete() // Delete the file if it exceeds 5MB
            logFile.createNewFile() // Optionally create a new empty file after deleting
            Log.i(tag, "new temp file created successfully")
        }
    }
}
