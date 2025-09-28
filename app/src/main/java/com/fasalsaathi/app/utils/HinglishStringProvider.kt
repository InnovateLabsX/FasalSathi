package com.fasalsaathi.app.utils

import android.content.Context
import com.fasalsaathi.app.R

/**
 * Provides Hinglish (Hindi + English) strings for the app
 * Since Android doesn't natively support custom locales like "hinglish",
 * we handle this programmatically
 */
class HinglishStringProvider(private val context: Context) {
    
    companion object {
        private val hinglishStrings = mapOf(
            // App Name
            R.string.app_name to "FasalSaathi",
            R.string.welcome_back to "Wapas Swagat Hai",
            
            // Dashboard
            R.string.dashboard to "Dashboard",
            R.string.crop_recommendation to "Fasal Suggestion",
            R.string.disease_detection to "Disease Detection",
            R.string.market_prices to "Market Rates",
            R.string.weather to "Mausam",
            R.string.profile to "Profile",
            R.string.settings to "Settings",
            R.string.support to "Help",
            
            // Dashboard specific
            R.string.todays_focus to "Aaj Ka Focus",
            R.string.smart_actions to "Smart Actions",
            R.string.smart_insights to "Smart Tips",
            R.string.view_reports to "Reports Dekho",
            R.string.dashboard_insight_tagline to "Aapke farm ke liye aaj ki smart tips",
            R.string.dashboard_manage_button to "Manage",
            R.string.dashboard_soil_moisture_chip to "Mitti ki nami 68%",
            R.string.dashboard_irrigation_chip to "2 ghante mein paani",
            
            // Focus areas
            R.string.focus_soil_health to "Mitti Health",
            R.string.focus_pest_watch to "Keede Watch",
            R.string.focus_market_rates to "Market Rates",
            R.string.focus_water_usage to "Paani Usage",
            
            // Actions
            R.string.action_ai_suggestion_subtitle to "AI tips lo",
            R.string.action_disease_scan_subtitle to "Leaves scan karo",
            R.string.action_profile_subtitle to "Account manage karo",
            R.string.action_ai_assistant_subtitle to "Farming advice lo",
            R.string.action_weather_subtitle to "Farm work plan karo",
            
            // Insights
            R.string.insight_soil_moisture to "Mitti Moisture",
            R.string.insight_soil_moisture_value to "68% • Best",
            R.string.insight_market_price to "Market Price",
            R.string.insight_market_price_delta to "Gehun ↑ 3.2%",
            R.string.insight_market_price_value to "₹ 2,150 / quintal",
            
            // Common actions
            R.string.ok to "OK",
            R.string.cancel to "Cancel",
            R.string.yes to "Haan",
            R.string.no to "Nahi",
            R.string.save to "Save",
            R.string.edit to "Edit",
            R.string.delete to "Delete",
            R.string.share to "Share",
            R.string.retry to "Phir Try Karo",
            R.string.loading to "Load Ho Raha Hai...",
            
            // Language
            R.string.select_language to "Language Choose Karo",
            R.string.english to "English",
            R.string.hindi to "Hindi",
            R.string.hinglish to "Hinglish"
        )
    }
    
    /**
     * Get Hinglish string for a given resource ID
     */
    fun getString(resourceId: Int): String? {
        return hinglishStrings[resourceId]
    }
    
    /**
     * Get Hinglish string with fallback to default
     */
    fun getStringWithFallback(resourceId: Int): String {
        return hinglishStrings[resourceId] ?: context.getString(resourceId)
    }
}