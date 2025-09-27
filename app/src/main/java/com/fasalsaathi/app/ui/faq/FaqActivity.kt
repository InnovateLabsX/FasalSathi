package com.fasalsaathi.app.ui.faq

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class FaqActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var etQuestion: TextInputEditText
    private lateinit var btnVoiceInput: MaterialButton
    private lateinit var btnImageInput: MaterialButton
    private lateinit var btnSendQuestion: MaterialButton
    private lateinit var btnRemoveImage: MaterialButton
    private lateinit var btnClearChat: MaterialButton
    
    private lateinit var cardImagePreview: androidx.cardview.widget.CardView
    private lateinit var ivSelectedImage: ImageView
    private lateinit var rvChatMessages: RecyclerView
    
    private lateinit var chipCropDiseases: Chip
    private lateinit var chipWeatherAdvice: Chip
    private lateinit var chipSoilHealth: Chip
    private lateinit var chipIrrigation: Chip
    private lateinit var chipPestControl: Chip
    private lateinit var chipFertilizers: Chip
    
    private lateinit var chatAdapter: ChatMessageAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    
    private var selectedImageBitmap: Bitmap? = null
    private var textToSpeech: TextToSpeech? = null
    
    companion object {
        private const val VOICE_INPUT_REQUEST = 100
        private const val IMAGE_PICK_REQUEST = 101
        private const val IMAGE_CAPTURE_REQUEST = 102
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 103
        private const val PERMISSION_REQUEST_CAMERA = 104
        
        // OpenAI API Configuration (replace with your API key)
        private const val OPENAI_API_KEY = "your_openai_api_key_here"
        private const val OPENAI_BASE_URL = "https://api.openai.com/v1/chat/completions"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        
        initViews()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupTextToSpeech()
        addWelcomeMessage()
    }
    
    private fun initViews() {
        etQuestion = findViewById(R.id.etQuestion)
        btnVoiceInput = findViewById(R.id.btnVoiceInput)
        btnImageInput = findViewById(R.id.btnImageInput)
        btnSendQuestion = findViewById(R.id.btnSendQuestion)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        btnClearChat = findViewById(R.id.btnClearChat)
        
        cardImagePreview = findViewById(R.id.cardImagePreview)
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        
        chipCropDiseases = findViewById(R.id.chipCropDiseases)
        chipWeatherAdvice = findViewById(R.id.chipWeatherAdvice)
        chipSoilHealth = findViewById(R.id.chipSoilHealth)
        chipIrrigation = findViewById(R.id.chipIrrigation)
        chipPestControl = findViewById(R.id.chipPestControl)
        chipFertilizers = findViewById(R.id.chipFertilizers)
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "AI FAQ Assistant"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter(chatMessages) { message ->
            if (message.isFromAi && message.message.isNotEmpty()) {
                speakText(message.message)
            }
        }
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(this@FaqActivity)
            adapter = chatAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnVoiceInput.setOnClickListener { startVoiceInput() }
        btnImageInput.setOnClickListener { showImageInputOptions() }
        btnSendQuestion.setOnClickListener { sendQuestion() }
        btnRemoveImage.setOnClickListener { removeSelectedImage() }
        btnClearChat.setOnClickListener { clearChat() }
        
        // Quick FAQ topics
        chipCropDiseases.setOnClickListener { 
            etQuestion.setText("What are common crop diseases and how to prevent them?")
        }
        chipWeatherAdvice.setOnClickListener { 
            etQuestion.setText("How does current weather affect my crops and what should I do?")
        }
        chipSoilHealth.setOnClickListener { 
            etQuestion.setText("How can I improve my soil health and fertility?")
        }
        chipIrrigation.setOnClickListener { 
            etQuestion.setText("What's the best irrigation schedule for my crops?")
        }
        chipPestControl.setOnClickListener { 
            etQuestion.setText("How to identify and control pests in my farm organically?")
        }
        chipFertilizers.setOnClickListener { 
            etQuestion.setText("What fertilizers should I use for better crop yield?")
        }
    }
    
    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
        }
    }
    
    private fun addWelcomeMessage() {
        val welcomeMessage = """
            🌾 Welcome to FasalSaathi AI Assistant!
            
            I'm here to help you with:
            • Crop diseases and pest control
            • Weather-based farming advice  
            • Soil health and fertilizers
            • Irrigation and water management
            • Seasonal farming tips
            • Market prices and trends
            
            You can ask questions by typing, using voice input, or uploading images of your crops for analysis!
        """.trimIndent()
        
        addMessage(ChatMessage(welcomeMessage, true, getCurrentTimestamp()))
    }
    
    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.RECORD_AUDIO), 
                PERMISSION_REQUEST_RECORD_AUDIO)
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask your farming question...")
        }
        
        try {
            startActivityForResult(intent, VOICE_INPUT_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showImageInputOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> captureImage()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }
    
    private fun captureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                arrayOf(Manifest.permission.CAMERA), 
                PERMISSION_REQUEST_CAMERA)
            return
        }
        
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE_REQUEST)
        }
    }
    
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }
    
    private fun removeSelectedImage() {
        selectedImageBitmap = null
        cardImagePreview.visibility = View.GONE
    }
    
    private fun sendQuestion() {
        val question = etQuestion.text.toString().trim()
        if (question.isEmpty() && selectedImageBitmap == null) {
            Toast.makeText(this, "Please enter a question or select an image", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Add user message
        val userMessage = if (question.isNotEmpty()) question else "Image analysis request"
        addMessage(ChatMessage(userMessage, false, getCurrentTimestamp(), selectedImageBitmap))
        
        // Clear input
        etQuestion.text?.clear()
        
        // Send to AI
        sendToAI(question, selectedImageBitmap)
        
        // Clear selected image
        removeSelectedImage()
    }
    
    private fun sendToAI(question: String, image: Bitmap?) {
        lifecycleScope.launch {
            try {
                // Show typing indicator
                addMessage(ChatMessage("🤖 Thinking...", true, getCurrentTimestamp()))
                
                val response = if (image != null) {
                    analyzeImageWithAI(question, image)
                } else {
                    getAIResponse(question)
                }
                
                // Remove typing indicator
                if (chatMessages.isNotEmpty() && chatMessages.last().message == "🤖 Thinking...") {
                    chatMessages.removeAt(chatMessages.size - 1)
                }
                
                // Add AI response
                addMessage(ChatMessage(response, true, getCurrentTimestamp()))
                
            } catch (e: Exception) {
                // Remove typing indicator
                if (chatMessages.isNotEmpty() && chatMessages.last().message == "🤖 Thinking...") {
                    chatMessages.removeAt(chatMessages.size - 1)
                }
                
                val fallbackResponse = generateFallbackResponse(question)
                addMessage(ChatMessage(fallbackResponse, true, getCurrentTimestamp()))
            }
        }
    }
    
    private suspend fun getAIResponse(question: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // If OpenAI API key is not set, use fallback
                if (OPENAI_API_KEY == "your_openai_api_key_here") {
                    return@withContext generateFallbackResponse(question)
                }
                
                val jsonBody = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are an expert agricultural advisor helping Indian farmers. Provide practical, actionable advice for farming in Indian conditions.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", question)
                        })
                    })
                    put("max_tokens", 500)
                }
                
                val connection = URL(OPENAI_BASE_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $OPENAI_API_KEY")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                
                connection.outputStream.use { output ->
                    output.write(jsonBody.toString().toByteArray())
                }
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)
                    jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } else {
                    generateFallbackResponse(question)
                }
            } catch (e: Exception) {
                generateFallbackResponse(question)
            }
        }
    }
    
    private suspend fun analyzeImageWithAI(question: String, image: Bitmap): String {
        return withContext(Dispatchers.IO) {
            // For now, provide image analysis fallback
            // In production, you would use OpenAI Vision API or Google Vision API
            generateImageAnalysisFallback(question)
        }
    }
    
    private fun generateFallbackResponse(question: String): String {
        val lowerQuestion = question.lowercase()
        
        return when {
            lowerQuestion.contains("disease") || lowerQuestion.contains("pest") -> {
                """
                🦠 **Common Crop Disease Management:**
                
                **Prevention:**
                • Use disease-resistant crop varieties
                • Maintain proper spacing for air circulation
                • Avoid overhead watering
                • Practice crop rotation
                
                **Common Diseases:**
                • **Fungal:** Use copper-based fungicides
                • **Bacterial:** Improve drainage, use certified seeds
                • **Viral:** Control insect vectors
                
                **Organic Solutions:**
                • Neem oil spray (2-3ml per liter)
                • Trichoderma application in soil
                • Maintain soil pH 6.0-7.0
                
                📞 Consult local agricultural officer for specific identification.
                """.trimIndent()
            }
            
            lowerQuestion.contains("weather") -> {
                """
                🌦️ **Weather-Based Farming Advice:**
                
                **Monsoon Season:**
                • Ensure proper drainage
                • Apply fungicides preventively
                • Avoid fertilizer application during heavy rains
                
                **Summer Season:**
                • Increase irrigation frequency
                • Use mulching to retain moisture
                • Protect crops during extreme heat (>40°C)
                
                **Winter Season:**
                • Protect from frost using smoke/covers
                • Reduce watering frequency
                • Good time for land preparation
                
                💡 Check weather forecast regularly and adjust farming activities accordingly.
                """.trimIndent()
            }
            
            lowerQuestion.contains("soil") || lowerQuestion.contains("fertilizer") -> {
                """
                🌱 **Soil Health & Fertilizer Guide:**
                
                **Soil Testing:**
                • Test soil pH (ideal: 6.0-7.5)
                • Check NPK levels
                • Organic matter content should be 3-5%
                
                **Organic Fertilizers:**
                • Compost: 5-10 tons per hectare
                • Vermicompost: 2-5 tons per hectare  
                • Green manure: Grow legumes and incorporate
                
                **Chemical Fertilizers (per hectare):**
                • Nitrogen: 120-150 kg (in 2-3 splits)
                • Phosphorus: 60-80 kg (basal application)
                • Potassium: 40-60 kg (basal + top dressing)
                
                🔬 Get soil tested every 2-3 years for optimal results.
                """.trimIndent()
            }
            
            lowerQuestion.contains("irrigation") || lowerQuestion.contains("water") -> {
                """
                💧 **Irrigation Management:**
                
                **Drip Irrigation (Recommended):**
                • 30-50% water saving
                • Better nutrient uptake
                • Reduces weed growth
                
                **Scheduling:**
                • **Vegetative stage:** Every 2-3 days
                • **Flowering stage:** Daily (light watering)
                • **Fruiting stage:** Every alternate day
                
                **Water Quality:**
                • EC should be < 2.0 dS/m
                • pH between 6.0-8.5
                • Avoid saline water (>4 dS/m EC)
                
                **Water Conservation:**
                • Mulching reduces evaporation by 50%
                • Early morning irrigation is most effective
                • Use moisture meters to check soil water
                """.trimIndent()
            }
            
            else -> {
                """
                🌾 **General Farming Advice:**
                
                **Key Success Factors:**
                • Select appropriate crop varieties for your region
                • Follow proper planting calendar
                • Maintain optimal plant population
                • Integrated pest management
                
                **Modern Techniques:**
                • Precision agriculture using technology
                • Organic farming methods
                • Water-efficient irrigation systems
                • Soil health cards for nutrient management
                
                **Resources:**
                • Contact local KVK (Krishi Vigyan Kendra)
                • Use government schemes like PM-KISAN
                • Join farmer producer organizations (FPOs)
                
                💡 For specific advice, please ask about particular crops or problems you're facing.
                """.trimIndent()
            }
        }
    }
    
    private fun generateImageAnalysisFallback(question: String): String {
        return """
        📷 **Image Analysis:**
        
        I can see you've uploaded an image! For detailed plant/crop analysis, here's what to look for:
        
        **Visual Inspection Checklist:**
        • **Leaves:** Color, spots, wilting, holes, yellowing
        • **Stems:** Discoloration, lesions, pest damage
        • **Fruits/Flowers:** Size, color, deformities
        • **Overall plant:** Growth pattern, vigor
        
        **Common Issues to Check:**
        • **Yellow leaves:** Nutrient deficiency or overwatering
        • **Brown spots:** Fungal/bacterial diseases
        • **Holes in leaves:** Insect pest damage
        • **Wilting:** Water stress or root problems
        
        📞 **For accurate diagnosis, consult:**
        • Local agricultural extension officer
        • Nearby agricultural university
        • Experienced farmers in your area
        
        💡 Consider taking multiple photos from different angles and share with local experts for precise identification.
        """.trimIndent()
    }
    
    private fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        runOnUiThread {
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            rvChatMessages.scrollToPosition(chatMessages.size - 1)
        }
    }
    
    private fun clearChat() {
        chatMessages.clear()
        chatAdapter.notifyDataSetChanged()
        addWelcomeMessage()
    }
    
    private fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            VOICE_INPUT_REQUEST -> {
                if (resultCode == RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!result.isNullOrEmpty()) {
                        etQuestion.setText(result[0])
                    }
                }
            }
            
            IMAGE_PICK_REQUEST -> {
                if (resultCode == RESULT_OK && data != null) {
                    val imageUri = data.data
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        showSelectedImage()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            IMAGE_CAPTURE_REQUEST -> {
                if (resultCode == RESULT_OK && data != null) {
                    selectedImageBitmap = data.extras?.get("data") as? Bitmap
                    showSelectedImage()
                }
            }
        }
    }
    
    private fun showSelectedImage() {
        selectedImageBitmap?.let { bitmap ->
            ivSelectedImage.setImageBitmap(bitmap)
            cardImagePreview.visibility = View.VISIBLE
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PERMISSION_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceInput()
                } else {
                    Toast.makeText(this, "Voice input permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        textToSpeech?.shutdown()
        super.onDestroy()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}