package com.rollerskater.app.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a saved crash log entry.
 */
data class CrashLogData(
    val timestamp: Long,
    val formattedDate: String,
    val exceptionMessage: String,
    val stackTrace: String
) {
    /**
     * Returns the full crash log formatted for display or sharing.
     */
    fun getFullLog(): String {
        return buildString {
            appendLine("=== RollerSkater Crash Report ===")
            appendLine("Date: $formattedDate")
            appendLine("Exception: $exceptionMessage")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(stackTrace)
        }
    }
}

/**
 * Custom UncaughtExceptionHandler that saves crash information to SharedPreferences
 * so it can be displayed to the user when the app restarts.
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    private const val PREFS_NAME = "crash_handler_prefs"
    private const val CRASH_LOGS_DIR = "crash_logs"
    private const val MAX_SAVED_LOG_FILES = 20
    private const val KEY_HAS_CRASH = "has_crash"
    private const val KEY_TIMESTAMP = "crash_timestamp"
    private const val KEY_EXCEPTION_MESSAGE = "crash_exception_message"
    private const val KEY_STACK_TRACE = "crash_stack_trace"

    @Volatile
    private lateinit var appContext: Context
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    private val prefs: SharedPreferences
        get() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Installs this crash handler as the default uncaught exception handler.
     * Should be called in Application.onCreate().
     */
    fun install(context: Context) {
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrashLog(throwable)
        } catch (e: Exception) {
        }

        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashLog(throwable: Throwable) {
        val timestamp = System.currentTimeMillis()
        val stackTrace = getStackTraceString(throwable)
        val exceptionMessage = throwable.message ?: throwable.javaClass.simpleName

        prefs.edit().apply {
            putBoolean(KEY_HAS_CRASH, true)
            putLong(KEY_TIMESTAMP, timestamp)
            putString(KEY_EXCEPTION_MESSAGE, exceptionMessage)
            putString(KEY_STACK_TRACE, stackTrace)
            commit()
        }

        writeCrashFile(
            timestamp = timestamp,
            tag = "UncaughtException",
            exceptionMessage = exceptionMessage,
            stackTrace = stackTrace
        )
    }

    /**
     * Builds a [CoroutineExceptionHandler] that logs unhandled coroutine errors via Timber
     * and writes them to a local-only file under `filesDir/crash_logs/` without any network calls.
     */
    fun coroutineExceptionHandler(tag: String): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Timber.tag(tag).e(throwable, "Unhandled coroutine exception in %s", tag)
            val timestamp = System.currentTimeMillis()
            writeCrashFile(
                timestamp = timestamp,
                tag = "CoroutineException/$tag",
                exceptionMessage = throwable.message ?: throwable.javaClass.simpleName,
                stackTrace = getStackTraceString(throwable)
            )
        }

    private fun writeCrashFile(
        timestamp: Long,
        tag: String,
        exceptionMessage: String,
        stackTrace: String
    ) {
        if (!::appContext.isInitialized) return
        runCatching {
            val dir = File(appContext.filesDir, CRASH_LOGS_DIR)
            if (!dir.exists()) dir.mkdirs()
            val fileStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date(timestamp))
            val humanDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
            val file = File(dir, "crash_$fileStamp.txt")
            file.writeText(
                buildString {
                    appendLine("=== RollerSkater Crash Report ($tag) ===")
                    appendLine("Date: $humanDate")
                    appendLine("Exception: $exceptionMessage")
                    appendLine()
                    appendLine("Stack Trace:")
                    appendLine(stackTrace)
                }
            )
            val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
            if (files.size > MAX_SAVED_LOG_FILES) {
                files.drop(MAX_SAVED_LOG_FILES).forEach { runCatching { it.delete() } }
            }
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    /**
     * Checks if there is a saved crash log from a previous session.
     */
    fun hasCrashLog(): Boolean {
        if (!::appContext.isInitialized) return false
        return prefs.getBoolean(KEY_HAS_CRASH, false)
    }

    /**
     * Retrieves the saved crash log data.
     * Returns null if no crash log exists.
     */
    fun getCrashLog(): CrashLogData? {
        if (!hasCrashLog()) return null

        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)
        val exceptionMessage = prefs.getString(KEY_EXCEPTION_MESSAGE, "Unknown error") ?: "Unknown error"
        val stackTrace = prefs.getString(KEY_STACK_TRACE, "") ?: ""

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(timestamp))

        return CrashLogData(
            timestamp = timestamp,
            formattedDate = formattedDate,
            exceptionMessage = exceptionMessage,
            stackTrace = stackTrace
        )
    }

    /**
     * Clears the saved crash log.
     * Should be called after the user has acknowledged the crash report.
     */
    fun clearCrashLog() {
        if (!::appContext.isInitialized) return
        prefs.edit().clear().apply()
    }
}
