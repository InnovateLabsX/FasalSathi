package com.fasalsaathi.app.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.fasalsaathi.app.R

/**
 * Utility class for accessibility improvements
 */
object AccessibilityUtils {
    
    /**
     * Set up accessibility for interactive views
     */
    fun setupAccessibility(view: View, description: String, role: String? = null) {
        ViewCompat.setAccessibilityDelegate(view, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.contentDescription = description
                role?.let { info.roleDescription = it }
                info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }
        })
    }
    
    /**
     * Set up accessibility for card views
     */
    fun setupCardAccessibility(cardView: View, title: String, description: String) {
        val fullDescription = "$title. $description. Double tap to open."
        setupAccessibility(cardView, fullDescription, "Button")
    }
    
    /**
     * Set up accessibility for navigation items
     */
    fun setupNavigationAccessibility(view: View, itemName: String, isSelected: Boolean = false) {
        val description = if (isSelected) {
            "$itemName tab, selected"
        } else {
            "$itemName tab, not selected. Double tap to navigate."
        }
        setupAccessibility(view, description, "Tab")
    }
    
    /**
     * Set up accessibility for form fields
     */
    fun setupFormFieldAccessibility(view: View, fieldName: String, hint: String? = null, isRequired: Boolean = false) {
        val description = buildString {
            append(fieldName)
            if (isRequired) append(", required")
            hint?.let { append(", $it") }
        }
        setupAccessibility(view, description, "Text field")
    }
    
    /**
     * Announce message for screen readers
     */
    fun announceForAccessibility(view: View, message: String) {
        view.announceForAccessibility(message)
    }
    
    /**
     * Set up accessibility for the entire view group
     */
    fun setupViewGroupAccessibility(viewGroup: ViewGroup, context: Context) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child.id) {
                R.id.cardCropRecommendation -> {
                    setupCardAccessibility(child, "Crop Recommendation", "Get AI-powered crop suggestions based on your soil and weather conditions")
                }
                R.id.cardDiseaseDetection -> {
                    setupCardAccessibility(child, "Disease Detection", "Use camera to detect crop diseases and get treatment recommendations")
                }
                R.id.cardAIAssistant -> {
                    setupCardAccessibility(child, "AI Assistant", "Chat with AI for farming advice and get voice-powered assistance")
                }
                R.id.cardWeather -> {
                    setupCardAccessibility(child, "Weather Details", "View detailed weather information and agricultural forecasts")
                }
                R.id.chipSoilHealth -> {
                    setupAccessibility(child, "Soil Health information. Double tap to learn about maintaining healthy soil.", "Button")
                }
                R.id.chipPestControl -> {
                    setupAccessibility(child, "Pest Control information. Double tap to learn about pest control strategies.", "Button")
                }
                R.id.chipIrrigation -> {
                    setupAccessibility(child, "Irrigation information. Double tap to learn about efficient irrigation.", "Button")
                }
            }
            
            if (child is ViewGroup) {
                setupViewGroupAccessibility(child, context)
            }
        }
    }
}