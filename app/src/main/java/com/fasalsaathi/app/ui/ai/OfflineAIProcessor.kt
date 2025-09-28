package com.fasalsaathi.app.ui.ai

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class OfflineAIProcessor(private val context: Context) {
    
    private var isInitialized = false
    private lateinit var agricultureKnowledgeBase: Map<String, List<String>>
    
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            loadAgriculturalKnowledgeBase()
            isInitialized = true
        } catch (e: Exception) {
            throw Exception("Failed to initialize offline AI: ${e.message}")
        }
    }
    
    private suspend fun loadAgriculturalKnowledgeBase() = withContext(Dispatchers.IO) {
        // Load pre-defined agricultural knowledge
        agricultureKnowledgeBase = mapOf(
            "crop_diseases" to listOf(
                "Leaf spot: Caused by fungi, treat with copper-based fungicides",
                "Powdery mildew: White powdery coating on leaves, improve air circulation",
                "Root rot: Caused by overwatering, improve drainage",
                "Bacterial blight: Dark spots on leaves, use disease-resistant varieties",
                "Rust: Orange/brown spots, apply preventive fungicides"
            ),
            "pest_control" to listOf(
                "Aphids: Use neem oil or introduce ladybugs as natural predators",
                "Caterpillars: Hand-picking or use Bt (Bacillus thuringiensis) spray",
                "Whiteflies: Yellow sticky traps and reflective mulches",
                "Thrips: Blue sticky traps and predatory mites",
                "Spider mites: Increase humidity and use predatory insects"
            ),
            "soil_management" to listOf(
                "Maintain soil pH between 6.0-7.0 for optimal nutrient uptake",
                "Add compost regularly to improve soil structure and fertility",
                "Practice crop rotation to prevent nutrient depletion",
                "Use cover crops to prevent soil erosion",
                "Test soil every 2-3 years for nutrient levels"
            ),
            "irrigation_tips" to listOf(
                "Water early morning (6-8 AM) or evening (6-8 PM)",
                "Check soil moisture 2-3 inches deep before watering",
                "Use drip irrigation for 30-50% water savings",
                "Apply mulch to reduce water evaporation",
                "Collect rainwater for sustainable irrigation"
            ),
            "fertilizer_advice" to listOf(
                "Use balanced NPK (10-10-10) for general crop nutrition",
                "Apply nitrogen during vegetative growth phase",
                "Phosphorus promotes root development and flowering",
                "Potassium improves disease resistance and fruit quality",
                "Organic fertilizers release nutrients slowly and improve soil health"
            ),
            "weather_farming" to listOf(
                "Plan planting dates based on last frost date",
                "Protect crops during temperature extremes",
                "Adjust irrigation based on rainfall predictions",
                "Use row covers during cold snaps",
                "Harvest before severe weather events"
            ),
            "crop_selection" to listOf(
                "Choose varieties suited to your climate zone",
                "Consider disease-resistant cultivars",
                "Plant cool-season crops in spring and fall",
                "Warm-season crops need soil temperature above 60°F",
                "Check days to maturity for harvest planning"
            ),
            "organic_farming" to listOf(
                "Use compost and organic matter for soil fertility",
                "Implement integrated pest management (IPM)",
                "Encourage beneficial insects with diverse plantings",
                "Avoid synthetic pesticides and fertilizers",
                "Practice crop rotation and companion planting"
            )
        )
    }
    
    suspend fun processQuestion(question: String): String = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext "AI processor not initialized. Please try again."
        }
        
        val processedQuestion = preprocessQuestion(question)
        val category = categorizeQuestion(processedQuestion)
        val response = generateResponse(processedQuestion, category)
        
        return@withContext formatResponse(response, category)
    }
    
    private fun preprocessQuestion(question: String): String {
        return question.toLowerCase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    private fun categorizeQuestion(question: String): String {
        val keywords = mapOf(
            "crop_diseases" to listOf("disease", "infection", "fungus", "bacteria", "virus", "spot", "blight", "rot", "mildew", "rust"),
            "pest_control" to listOf("pest", "insect", "bug", "aphid", "caterpillar", "worm", "fly", "mite", "control"),
            "soil_management" to listOf("soil", "earth", "ground", "ph", "nutrient", "fertilizer", "compost", "organic"),
            "irrigation_tips" to listOf("water", "irrigation", "watering", "drought", "moisture", "dry", "wet"),
            "weather_farming" to listOf("weather", "rain", "temperature", "frost", "heat", "cold", "season"),
            "crop_selection" to listOf("crop", "plant", "variety", "cultivar", "seed", "planting", "grow"),
            "organic_farming" to listOf("organic", "natural", "sustainable", "eco", "biological")
        )
        
        val scores = keywords.mapValues { (_, keywordList) ->
            keywordList.count { keyword -> question.contains(keyword) }
        }
        
        return scores.maxByOrNull { it.value }?.key ?: "general_farming"
    }
    
    private fun generateResponse(question: String, category: String): String {
        val knowledgeItems = agricultureKnowledgeBase[category] ?: return generateGeneralResponse(question)
        
        // Find most relevant knowledge items
        val relevantItems = knowledgeItems.filter { item ->
            val itemWords = item.toLowerCase().split(" ")
            val questionWords = question.split(" ")
            itemWords.any { word -> questionWords.contains(word) }
        }
        
        return if (relevantItems.isNotEmpty()) {
            relevantItems.take(3).joinToString("\n\n• ")
        } else {
            knowledgeItems.shuffled().take(2).joinToString("\n\n• ")
        }
    }
    
    private fun formatResponse(response: String, category: String): String {
        val emoji = when (category) {
            "crop_diseases" -> "🦠"
            "pest_control" -> "🐛"
            "soil_management" -> "🌱"
            "irrigation_tips" -> "💧"
            "weather_farming" -> "🌤️"
            "crop_selection" -> "🌾"
            "organic_farming" -> "🌿"
            else -> "🚜"
        }
        
        val title = when (category) {
            "crop_diseases" -> "Disease Management"
            "pest_control" -> "Pest Control"
            "soil_management" -> "Soil Management"
            "irrigation_tips" -> "Irrigation Guidance"
            "weather_farming" -> "Weather-Based Farming"
            "crop_selection" -> "Crop Selection"
            "organic_farming" -> "Organic Farming"
            else -> "Farming Advice"
        }
        
        return """
            $emoji $title:
            
            • $response
            
            💡 Tip: For specific issues, consider consulting with local agricultural extension services or taking soil/plant samples for testing.
        """.trimIndent()
    }
    
    private fun generateGeneralResponse(question: String): String {
        val generalAdvice = listOf(
            "Regular monitoring of crops helps detect problems early",
            "Healthy soil is the foundation of successful farming",
            "Water management is crucial for crop productivity",
            "Choose crops suitable for your local climate",
            "Integrated pest management reduces chemical dependency",
            "Proper timing of farming activities increases yields"
        )
        
        return generalAdvice.shuffled().take(2).joinToString("\n\n• ")
    }
}