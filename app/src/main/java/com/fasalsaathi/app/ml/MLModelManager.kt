package com.fasalsaathi.app.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.util.concurrent.TimeUnit

/**
 * ML Integration for Fasal Sathi App
 * Interfaces with Python ML models for crop recommendation and soil type prediction
 */
class MLModelManager(private val context: Context) {
    
    data class CropPrediction(
        val recommendedCrop: String,
        val confidence: Double,
        val topRecommendations: List<CropRecommendationItem>,
        val success: Boolean,
        val error: String? = null
    )
    
    data class CropRecommendationItem(
        val crop: String,
        val confidence: Double
    )
    
    data class SoilTypePrediction(
        val soilType: String,
        val confidence: Double,
        val topPredictions: List<SoilTypeItem>,
        val success: Boolean,
        val error: String? = null
    )
    
    data class SoilTypeItem(
        val soilType: String,
        val confidence: Double
    )
    
    data class SoilData(
        val n: Double,           // Nitrogen
        val p: Double,           // Phosphorus
        val k: Double,           // Potassium
        val ph: Double,          // pH level
        val temperature: Double, // Temperature in Celsius
        val humidity: Double,    // Humidity percentage
        val rainfall: Double,    // Rainfall in mm
        val ec: Double? = null,  // Electrical Conductivity
        val oc: Double? = null,  // Organic Carbon
        val s: Double? = null,   // Sulfur
        val zn: Double? = null,  // Zinc
        val fe: Double? = null,  // Iron
        val cu: Double? = null,  // Copper
        val mn: Double? = null,  // Manganese
        val b: Double? = null    // Boron
    )
    
    companion object {
        private const val PYTHON_SCRIPT_PATH = "/data/data/com.fasalsaathi.app/files/ml/"
        private const val MODEL_PREDICTION_TIMEOUT = 30L // seconds
    }
    
    /**
     * Predict crop recommendation based on soil and environmental data
     */
    suspend fun predictCrop(soilData: SoilData): CropPrediction = withContext(Dispatchers.IO) {
        try {
            val inputJson = createInputJson(soilData)
            val result = executePythonPrediction("crop", inputJson)
            parseCropPrediction(result)
        } catch (e: Exception) {
            CropPrediction("", 0.0, emptyList(), false, e.message)
        }
    }
    
    /**
     * Predict soil type based on soil parameters
     */
    suspend fun predictSoilType(soilData: SoilData): SoilTypePrediction = withContext(Dispatchers.IO) {
        try {
            val inputJson = createInputJson(soilData)
            val result = executePythonPrediction("soil", inputJson)
            parseSoilTypePrediction(result)
        } catch (e: Exception) {
            SoilTypePrediction("", 0.0, emptyList(), false, e.message)
        }
    }
    
    /**
     * Get crop recommendation with fallback to rule-based system
     */
    suspend fun getCropRecommendationWithFallback(soilData: SoilData): CropPrediction {
        // Try ML prediction first
        val mlPrediction = predictCrop(soilData)
        
        if (mlPrediction.success && mlPrediction.confidence > 0.3) {
            return mlPrediction
        }
        
        // Fallback to rule-based system
        return getRuleBasedCropRecommendation(soilData)
    }
    
    /**
     * Rule-based crop recommendation as fallback
     */
    private fun getRuleBasedCropRecommendation(soilData: SoilData): CropPrediction {
        val recommendations = mutableListOf<CropRecommendationItem>()
        
        // Simple rule-based logic
        when {
            soilData.rainfall > 1000 && soilData.humidity > 75 -> {
                recommendations.add(CropRecommendationItem("rice", 0.8))
                recommendations.add(CropRecommendationItem("sugarcane", 0.7))
                recommendations.add(CropRecommendationItem("jute", 0.6))
            }
            soilData.temperature > 25 && soilData.rainfall < 600 -> {
                recommendations.add(CropRecommendationItem("cotton", 0.75))
                recommendations.add(CropRecommendationItem("groundnut", 0.7))
                recommendations.add(CropRecommendationItem("mustard", 0.65))
            }
            soilData.ph > 7.0 && soilData.temperature < 20 -> {
                recommendations.add(CropRecommendationItem("wheat", 0.8))
                recommendations.add(CropRecommendationItem("chickpea", 0.7))
                recommendations.add(CropRecommendationItem("lentil", 0.65))
            }
            else -> {
                recommendations.add(CropRecommendationItem("maize", 0.7))
                recommendations.add(CropRecommendationItem("wheat", 0.65))
                recommendations.add(CropRecommendationItem("rice", 0.6))
            }
        }
        
        return CropPrediction(
            recommendedCrop = recommendations.firstOrNull()?.crop ?: "maize",
            confidence = recommendations.firstOrNull()?.confidence ?: 0.7,
            topRecommendations = recommendations,
            success = true
        )
    }
    
    /**
     * Create JSON input for Python script
     */
    private fun createInputJson(soilData: SoilData): String {
        val jsonObject = JSONObject().apply {
            put("n", soilData.n)
            put("p", soilData.p)
            put("k", soilData.k)
            put("ph", soilData.ph)
            put("temperature", soilData.temperature)
            put("humidity", soilData.humidity)
            put("rainfall", soilData.rainfall)
            
            // Optional parameters
            soilData.ec?.let { put("ec", it) }
            soilData.oc?.let { put("oc", it) }
            soilData.s?.let { put("s", it) }
            soilData.zn?.let { put("zn", it) }
            soilData.fe?.let { put("fe", it) }
            soilData.cu?.let { put("cu", it) }
            soilData.mn?.let { put("mn", it) }
            soilData.b?.let { put("b", it) }
        }
        return jsonObject.toString()
    }
    
    /**
     * Execute Python prediction script
     */
    private suspend fun executePythonPrediction(predictionType: String, inputJson: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // For now, simulate Python execution
                // In production, this would call the Python script with the models
                simulatePrediction(predictionType, inputJson)
            } catch (e: Exception) {
                throw RuntimeException("Failed to execute prediction: ${e.message}")
            }
        }
    }
    
    /**
     * Simulate prediction for testing (replace with actual Python execution)
     */
    private fun simulatePrediction(predictionType: String, inputJson: String): String {
        val inputData = JSONObject(inputJson)
        
        return when (predictionType) {
            "crop" -> {
                val rainfall = inputData.optDouble("rainfall", 0.0)
                val temperature = inputData.optDouble("temperature", 0.0)
                val humidity = inputData.optDouble("humidity", 0.0)
                
                val crop = when {
                    rainfall > 1000 -> "rice"
                    temperature > 25 && rainfall < 600 -> "cotton"
                    temperature < 20 -> "wheat"
                    else -> "maize"
                }
                
                """
                {
                    "recommended_crop": "$crop",
                    "confidence": 0.75,
                    "top_recommendations": [
                        {"crop": "$crop", "confidence": 0.75},
                        {"crop": "wheat", "confidence": 0.65},
                        {"crop": "rice", "confidence": 0.60}
                    ],
                    "success": true
                }
                """.trimIndent()
            }
            "soil" -> {
                val ph = inputData.optDouble("ph", 7.0)
                val soilType = when {
                    ph < 6.0 -> "Red"
                    ph > 8.0 -> "Black"
                    else -> "Alluvial"
                }
                
                """
                {
                    "soil_type": "$soilType",
                    "confidence": 0.70,
                    "top_predictions": [
                        {"soil_type": "$soilType", "confidence": 0.70},
                        {"soil_type": "Laterite", "confidence": 0.60},
                        {"soil_type": "Red", "confidence": 0.55}
                    ],
                    "success": true
                }
                """.trimIndent()
            }
            else -> """{"success": false, "error": "Unknown prediction type"}"""
        }
    }
    
    /**
     * Parse crop prediction result from JSON
     */
    private fun parseCropPrediction(jsonResult: String): CropPrediction {
        val json = JSONObject(jsonResult)
        
        if (!json.optBoolean("success", false)) {
            return CropPrediction("", 0.0, emptyList(), false, json.optString("error"))
        }
        
        val topRecommendations = mutableListOf<CropRecommendationItem>()
        val topRecsArray = json.optJSONArray("top_recommendations")
        
        topRecsArray?.let { array ->
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                topRecommendations.add(
                    CropRecommendationItem(
                        crop = item.getString("crop"),
                        confidence = item.getDouble("confidence")
                    )
                )
            }
        }
        
        return CropPrediction(
            recommendedCrop = json.optString("recommended_crop", ""),
            confidence = json.optDouble("confidence", 0.0),
            topRecommendations = topRecommendations,
            success = true
        )
    }
    
    /**
     * Parse soil type prediction result from JSON
     */
    private fun parseSoilTypePrediction(jsonResult: String): SoilTypePrediction {
        val json = JSONObject(jsonResult)
        
        if (!json.optBoolean("success", false)) {
            return SoilTypePrediction("", 0.0, emptyList(), false, json.optString("error"))
        }
        
        val topPredictions = mutableListOf<SoilTypeItem>()
        val topPredsArray = json.optJSONArray("top_predictions")
        
        topPredsArray?.let { array ->
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                topPredictions.add(
                    SoilTypeItem(
                        soilType = item.getString("soil_type"),
                        confidence = item.getDouble("confidence")
                    )
                )
            }
        }
        
        return SoilTypePrediction(
            soilType = json.optString("soil_type", ""),
            confidence = json.optDouble("confidence", 0.0),
            topPredictions = topPredictions,
            success = true
        )
    }
}