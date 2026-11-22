package com.rollerskater.app

import android.util.Log
import timber.log.Timber

/**
 * A release-optimized Timber Tree that:
 * - Only logs WARN, ERROR, and WTF (suppresses VERBOSE, DEBUG, and INFO)
 * - Strips method/line information for performance
 */
class ReleaseTree : Timber.Tree() {
    
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.WARN
    }
    
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        
        val sanitizedMessage = sanitize(message)
        when (priority) {
            Log.WARN -> Log.w(tag, sanitizedMessage, t)
            Log.ERROR -> Log.e(tag, sanitizedMessage, t)
            Log.ASSERT -> Log.wtf(tag, sanitizedMessage, t)
        }
    }

    private fun sanitize(message: String): String {
        return message
            .replace(SENSITIVE_KEY_REGEX, "$1=***")
            .replace(AUTH_HEADER_REGEX, "$1 ***")
    }

    companion object {
        private val SENSITIVE_KEY_REGEX = Regex("(?i)(api[_-]?key|token|secret|password|access[_-]?token)=[^&\\s]+")
        private val AUTH_HEADER_REGEX = Regex("(?i)(bearer|basic)\\s+[A-Za-z0-9+/=._-]+")
    }
}
