package com.example.sportsorganizer.data.local.session

import android.content.Context
import androidx.core.content.edit

/**
 * Manages user session data using SharedPreferences.
 *
 * This class handles storing and retrieving the logged-in user's ID,
 * enabling session persistence across app restarts.
 *
 * @property context The Android context used to access SharedPreferences
 */
class SessionManager(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Saves the logged-in user's ID to persistent storage.
     *
     * @param id The unique identifier of the logged-in user
     */
    fun saveLoggedInUserId(id: Long) {
        prefs.edit { putLong(KEY_USER_ID, id) }
    }

    /**
     * Retrieves the logged-in user's ID from persistent storage.
     *
     * @return The user ID if a session exists, `null` otherwise
     */
    fun getLoggedInUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, NO_USER)
        return if (id == NO_USER) null else id
    }

    /**
     * Clears the current user session.
     *
     * This effectively logs the user out by removing their stored ID.
     */
    fun clearSession() {
        prefs.edit { remove(KEY_USER_ID) }
    }

    companion object {
        /** Name of the SharedPreferences file */
        private const val PREFS_NAME = "session_prefs"

        /** Key for storing the user ID */
        private const val KEY_USER_ID = "logged_in_user_id"

        /** Sentinel value indicating no user is logged in */
        private const val NO_USER = -1L
    }
}
