package com.fasalsaathi.app.ui.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class VoiceVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 8f
        style = Paint.Style.FILL
    }
    
    private val bars = mutableListOf<Float>()
    private val maxBars = 20
    private var animationRunning = false
    private var currentAmplitude = 0f
    
    private val animationRunnable = object : Runnable {
        override fun run() {
            if (animationRunning) {
                updateBars()
                invalidate()
                postDelayed(this, 100) // Update every 100ms
            }
        }
    }

    init {
        // Initialize bars with random heights
        repeat(maxBars) {
            bars.add(0.1f + Math.random().toFloat() * 0.3f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val barWidth = width / maxBars.toFloat()
        val centerY = height / 2f
        
        bars.forEachIndexed { index, height ->
            val left = index * barWidth + barWidth * 0.2f
            val right = left + barWidth * 0.6f
            val barHeight = height * centerY
            
            val top = centerY - barHeight
            val bottom = centerY + barHeight
            
            // Add gradient effect
            val alpha = (255 * (1f - height)).toInt().coerceIn(100, 255)
            paint.alpha = alpha
            
            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, paint)
        }
    }
    
    fun startAnimation() {
        animationRunning = true
        post(animationRunnable)
    }
    
    fun stopAnimation() {
        animationRunning = false
        removeCallbacks(animationRunnable)
        
        // Animate bars to minimum height
        bars.forEachIndexed { index, _ ->
            bars[index] = 0.1f
        }
        invalidate()
    }
    
    fun updateAmplitude(amplitude: Float) {
        currentAmplitude = amplitude.coerceIn(-40f, 0f) // Typical dB range
    }
    
    private fun updateBars() {
        // Shift bars to the left
        for (i in 0 until maxBars - 1) {
            bars[i] = bars[i + 1]
        }
        
        // Add new bar based on amplitude and some randomness
        val normalizedAmplitude = (currentAmplitude + 40f) / 40f // Normalize to 0-1
        val newHeight = 0.2f + normalizedAmplitude * 0.6f + (Math.random().toFloat() * 0.2f)
        bars[maxBars - 1] = newHeight.coerceIn(0.1f, 1f)
    }
}