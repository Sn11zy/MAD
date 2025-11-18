package com.example.sportsorganizer.data.local.session

import android.content.Context
import androidx.core.content.edit

// only for production use
class SessionManager(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLoggedInUserId(id: Long) {
        prefs.edit { putLong(KEY_USER_ID, id) }
    }

    fun getLoggedInUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, NO_USER)
        return if (id == NO_USER) null else id
    }

    fun clearSession() {
        prefs.edit { remove(KEY_USER_ID) }
    }

    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_USER_ID = "logged_in_user_id"
        private const val NO_USER = -1L
    }
}
