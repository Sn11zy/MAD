package com.example.sportsorganizer.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing the application's theme state.
 *
 * Provides reactive state for dark/light theme preference and allows
 * toggling between themes throughout the app lifecycle.
 */
class ThemeViewModel : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(false)

    /**
     * Observable state flow indicating whether dark theme is enabled.
     *
     * Collect this flow in composables to react to theme changes.
     */
    val isDarkTheme = _isDarkTheme.asStateFlow()

    /**
     * Toggles between dark and light theme.
     *
     * Switches the theme state and notifies all observers.
     */
    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }
}
