package com.fasalsaathi.app.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

class LanguageManager(private val context: Context) {
    
    companion object {
        const val LANGUAGE_ENGLISH = "english"
        const val LANGUAGE_HINDI = "hindi" 
        const val LANGUAGE_HINGLISH = "hinglish"
        const val PREF_LANGUAGE = "app_language"
    }
    
    private val sharedPreferences = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    
    fun setLocale(language: String) {
        val locale = when (language) {
            LANGUAGE_HINDI -> Locale("hi")
            LANGUAGE_HINGLISH -> Locale("hi") // We'll handle Hinglish at string level
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Save language preference
        sharedPreferences.edit()
            .putString(PREF_LANGUAGE, language)
            .apply()
    }
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(PREF_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language(LANGUAGE_ENGLISH, "English", "🇺🇸"),
            Language(LANGUAGE_HINDI, "हिंदी", "🇮🇳"),
            Language(LANGUAGE_HINGLISH, "Hinglish", "🇮🇳")
        )
    }
    
    data class Language(
        val code: String,
        val name: String,
        val flag: String
    )
}