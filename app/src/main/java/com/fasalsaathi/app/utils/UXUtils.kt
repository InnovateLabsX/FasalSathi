package com.fasalsaathi.app.utils

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.fasalsaathi.app.R

/**
 * Utility class for common UI operations and user experience improvements
 */
object UXUtils {
    
    /**
     * Show a styled success message
     */
    fun showSuccessMessage(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show a styled error message
     */
    fun showErrorMessage(context: Context, message: String) {
        Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
    }
    
    /**
     * Show a styled info message
     */
    fun showInfoMessage(context: Context, message: String) {
        Toast.makeText(context, "ℹ️ $message", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show a styled warning message
     */
    fun showWarningMessage(context: Context, message: String) {
        Toast.makeText(context, "⚠️ $message", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show a custom snackbar with action
     */
    fun showSnackbarWithAction(
        view: View,
        message: String,
        actionText: String,
        actionCallback: () -> Unit
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setAction(actionText) { actionCallback() }
        snackbar.setBackgroundTint(ContextCompat.getColor(view.context, R.color.primary_green))
        snackbar.setTextColor(Color.WHITE)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.show()
    }
    
    /**
     * Hide keyboard
     */
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
    
    /**
     * Show view with fade-in animation
     */
    fun fadeInView(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null)
    }
    
    /**
     * Hide view with fade-out animation
     */
    fun fadeOutView(view: View, duration: Long = 300) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                view.visibility = View.GONE
                view.alpha = 1f
            }
    }
    
    /**
     * Animate view scale (for button press effects)
     */
    fun animateButtonPress(view: View) {
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 0.95f)
        scaleDown.duration = 100
        scaleDown.repeatCount = 1
        scaleDown.repeatMode = ObjectAnimator.REVERSE
        scaleDown.start()
        
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f)
        scaleDownY.duration = 100
        scaleDownY.repeatCount = 1
        scaleDownY.repeatMode = ObjectAnimator.REVERSE
        scaleDownY.start()
    }
    
    /**
     * Set loading state for a view group
     */
    fun setLoadingState(container: ViewGroup, isLoading: Boolean) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            child.isEnabled = !isLoading
            child.alpha = if (isLoading) 0.6f else 1.0f
        }
    }
    
    /**
     * Smooth scroll to top for any scrollable view
     */
    fun smoothScrollToTop(scrollView: View) {
        when (scrollView) {
            is androidx.core.widget.NestedScrollView -> {
                scrollView.smoothScrollTo(0, 0)
            }
            is android.widget.ScrollView -> {
                scrollView.smoothScrollTo(0, 0)
            }
            is androidx.recyclerview.widget.RecyclerView -> {
                scrollView.smoothScrollToPosition(0)
            }
        }
    }
    
    /**
     * Add haptic feedback for user interactions
     */
    fun addHapticFeedback(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
    
    /**
     * Set up click listener with animations and haptic feedback
     */
    fun setUpInteractiveView(view: View, onClick: () -> Unit) {
        view.setOnClickListener {
            animateButtonPress(view)
            addHapticFeedback(view)
            onClick()
        }
    }
}