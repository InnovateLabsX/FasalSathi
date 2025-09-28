package com.fasalsaathi.app.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

class LanguageManager(private val context: Context) {
    
    companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_HINDI = "hi" 
        const val LANGUAGE_HINGLISH = "hinglish"
        const val LANGUAGE_MARATHI = "mr"
        const val LANGUAGE_GUJARATI = "gu"
        const val LANGUAGE_URDU = "ur"
        const val PREF_LANGUAGE = "app_language"
    }
    
    private val sharedPreferences = context.getSharedPreferences("fasalsaathi_prefs", Context.MODE_PRIVATE)
    private val hinglishProvider = HinglishStringProvider(context)
    
    fun setLanguage(languageCode: String) {
        setLocale(languageCode)
    }
    
    fun setLocale(language: String) {
        val locale = when (language) {
            LANGUAGE_HINDI -> Locale("hi", "IN")
            LANGUAGE_MARATHI -> Locale("mr", "IN")
            LANGUAGE_GUJARATI -> Locale("gu", "IN")
            LANGUAGE_URDU -> Locale("ur", "IN")
            LANGUAGE_HINGLISH -> {
                // For Hinglish, we'll use English locale but track it separately
                // The actual string switching will be handled at runtime
                Locale("en", "IN")
            }
            else -> Locale("en", "IN")
        }
        
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // For Android 7.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
        
        // Save language preference
        sharedPreferences.edit()
            .putString(PREF_LANGUAGE, language)
            .apply()
    }
    
    /**
     * Get string with proper language support
     * For Hinglish, returns custom strings; for others, uses system resources
     */
    fun getString(resourceId: Int): String {
        return when (getCurrentLanguage()) {
            LANGUAGE_HINGLISH -> hinglishProvider.getStringWithFallback(resourceId)
            else -> context.getString(resourceId)
        }
    }
    
    /**
     * Check if current language is Hinglish
     */
    fun isHinglish(): Boolean {
        return getCurrentLanguage() == LANGUAGE_HINGLISH
    }
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(PREF_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language(LANGUAGE_ENGLISH, "English", "��"),
            Language(LANGUAGE_HINDI, "हिंदी", "🇮🇳"),
            Language(LANGUAGE_MARATHI, "मराठी", "🇮🇳"),
            Language(LANGUAGE_GUJARATI, "ગુજરાતી", "🇮🇳"),
            Language(LANGUAGE_URDU, "اردو", "🇮🇳"),
            Language(LANGUAGE_HINGLISH, "Hinglish", "🇮🇳")
        )
    }
    
    data class Language(
        val code: String,
        val name: String,
        val flag: String
    )
}