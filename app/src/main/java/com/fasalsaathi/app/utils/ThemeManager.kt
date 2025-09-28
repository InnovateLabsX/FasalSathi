package com.fasalsaathi.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {
    
    companion object {
        const val PREF_THEME = "app_theme"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_AUTO = "auto"
        
        private const val PREFS_NAME = "fasalsaathi_prefs"
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Set the app theme
     * @param theme: "light", "dark", or "auto"
     */
    fun setTheme(theme: String) {
        // Save preference
        sharedPreferences.edit()
            .putString(PREF_THEME, theme)
            .apply()
        
        // Apply theme immediately
        applyTheme(theme)
    }
    
    /**
     * Get current theme setting
     */
    fun getCurrentTheme(): String {
        return sharedPreferences.getString(PREF_THEME, THEME_AUTO) ?: THEME_AUTO
    }
    
    /**
     * Check if dark mode is enabled
     */
    fun isDarkModeEnabled(): Boolean {
        return when (getCurrentTheme()) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_AUTO -> {
                // Check system setting
                val nightMode = context.resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
    }
    
    /**
     * Apply theme using AppCompatDelegate
     */
    fun applyTheme(theme: String = getCurrentTheme()) {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            THEME_AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Initialize theme on app startup
     */
    fun initializeTheme() {
        applyTheme()
    }
    
    /**
     * Toggle between light and dark mode
     */
    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            THEME_LIGHT -> THEME_DARK
            THEME_DARK -> THEME_LIGHT
            THEME_AUTO -> if (isDarkModeEnabled()) THEME_LIGHT else THEME_DARK
            else -> THEME_DARK
        }
        setTheme(newTheme)
    }
    
    /**
     * Get theme display name for UI
     */
    fun getThemeDisplayName(theme: String = getCurrentTheme()): String {
        return when (theme) {
            THEME_LIGHT -> "Light"
            THEME_DARK -> "Dark" 
            THEME_AUTO -> "Auto (System)"
            else -> "Auto (System)"
        }
    }
}
