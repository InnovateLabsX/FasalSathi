package com.fasalsaathi.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for performance optimizations
 */
object PerformanceUtils {
    
    /**
     * Load image efficiently with memory optimization
     */
    fun loadImageEfficiently(
        imageView: ImageView,
        imageUrl: String,
        lifecycleOwner: LifecycleOwner,
        placeholderResId: Int? = null,
        errorResId: Int? = null
    ) {
        // Set placeholder if provided
        placeholderResId?.let { imageView.setImageResource(it) }
        
        lifecycleOwner.lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    loadBitmapFromUrl(imageUrl, imageView.width, imageView.height)
                }
                
                // Set the loaded bitmap on the main thread
                withContext(Dispatchers.Main) {
                    bitmap?.let { imageView.setImageBitmap(it) }
                        ?: errorResId?.let { imageView.setImageResource(it) }
                }
            } catch (e: Exception) {
                // Handle error on main thread
                withContext(Dispatchers.Main) {
                    errorResId?.let { imageView.setImageResource(it) }
                }
            }
        }
    }
    
    /**
     * Load bitmap from URL with size optimization
     */
    private suspend fun loadBitmapFromUrl(url: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                
                // First, get image dimensions
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()
                
                // Calculate sample size for memory efficiency
                options.inSampleSize = calculateSampleSize(options, targetWidth, targetHeight)
                options.inJustDecodeBounds = false
                
                // Load the scaled bitmap
                val newConnection = URL(url).openConnection() as HttpURLConnection
                newConnection.connect()
                val newInputStream = newConnection.inputStream
                val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
                newInputStream.close()
                
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Calculate optimal sample size for image loading
     */
    private fun calculateSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Memory-efficient string operations
     */
    fun formatLargeText(text: String, maxLength: Int = 1000): String {
        return if (text.length > maxLength) {
            "${text.substring(0, maxLength)}..."
        } else text
    }
    
    /**
     * Debounce function calls to prevent rapid successive calls
     */
    class Debouncer(private val delayMs: Long) {
        private var lastCallTime = 0L
        
        fun call(action: () -> Unit) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCallTime >= delayMs) {
                lastCallTime = currentTime
                action()
            }
        }
    }
    
    /**
     * Cache for small data to reduce repeated calculations
     */
    object SimpleCache {
        private val cache = mutableMapOf<String, Any>()
        
        @Suppress("UNCHECKED_CAST")
        fun <T> get(key: String): T? = cache[key] as? T
        
        fun <T> put(key: String, value: T) {
            cache[key] = value as Any
        }
        
        fun clear() = cache.clear()
        
        fun remove(key: String) = cache.remove(key)
    }
    
    /**
     * Preload critical app data for better performance
     */
    fun preloadCriticalData(context: Context) {
        // This can be called in Application class or splash screen
        // to preload frequently used data
        
        // Example: Preload user preferences
        SimpleCache.put("app_theme", "default")
        SimpleCache.put("user_language", "en")
        
        // Add more preloading logic as needed
    }
}