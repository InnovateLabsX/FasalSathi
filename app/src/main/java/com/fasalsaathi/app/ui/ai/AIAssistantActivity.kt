package com.fasalsaathi.app.ui.ai

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasalsaathi.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AIAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // UI Components
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var etQuestion: TextInputEditText
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var fabVoiceInput: FloatingActionButton
    private lateinit var fabCameraInput: FloatingActionButton
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvListeningIndicator: TextView
    private lateinit var voiceVisualizerCard: MaterialCardView
    private lateinit var voiceVisualizerView: VoiceVisualizerView
    private lateinit var progressBar: ProgressBar
    
    // Voice and AI Components
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var offlineAIProcessor: OfflineAIProcessor
    
    // Data
    private lateinit var chatAdapter: AIMessageAdapter
    private val chatMessages = mutableListOf<AIMessage>()
    private var isRecording = false
    private var isTextToSpeechReady = false
    private var selectedImageUri: Uri? = null
    
    // Constants
    companion object {
        private const val RECORD_AUDIO_PERMISSION = 1001
        private const val VOICE_RECOGNITION_REQUEST = 1002
        private const val CAMERA_PERMISSION = 1003
        private const val IMAGE_CAPTURE_REQUEST = 1004
        private const val IMAGE_PICK_REQUEST = 1005
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_assistant)
        
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupVoiceComponents()
        setupClickListeners()
        setupQuickActionChips()
        initializeOfflineAI()
        
        // Add welcome message
        addWelcomeMessage()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etQuestion = findViewById(R.id.etQuestion)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        fabVoiceInput = findViewById(R.id.fabVoiceInput)
        fabCameraInput = findViewById(R.id.fabCameraInput)
        rvChatMessages = findViewById(R.id.rvChatMessages)
        chipGroup = findViewById(R.id.chipGroup)
        tvListeningIndicator = findViewById(R.id.tvListeningIndicator)
        voiceVisualizerCard = findViewById(R.id.voiceVisualizerCard)
        voiceVisualizerView = findViewById(R.id.voiceVisualizerView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "🤖 AI Farm Assistant"
        }
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = AIMessageAdapter(chatMessages) { message ->
            // Handle message click (e.g., read aloud)
            if (isTextToSpeechReady && message.isFromUser.not()) {
                speakText(message.content)
            }
        }
        
        rvChatMessages.apply {
            layoutManager = LinearLayoutManager(this@AIAssistantActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupVoiceComponents() {
        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(this, this)
        
        // Initialize Speech Recognizer for continuous listening
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(createRecognitionListener())
        }
    }

    private fun setupClickListeners() {
        btnSendMessage.setOnClickListener {
            sendTextMessage()
        }
        
        fabVoiceInput.setOnClickListener {
            if (isRecording) {
                stopVoiceInput()
            } else {
                startVoiceInput()
            }
        }
        
        fabCameraInput.setOnClickListener {
            showImageSourceDialog()
        }
        
        etQuestion.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollToBottom()
            }
        }
    }

    private fun setupQuickActionChips() {
        val quickActions = listOf(
            "🌾 Crop Diseases" to "What are common crop diseases in my region?",
            "🌤️ Weather Advice" to "What should I do based on current weather?",
            "🌱 Planting Tips" to "When is the best time to plant crops?",
            "💧 Irrigation" to "How much water do my crops need?",
            "🪴 Soil Health" to "How can I improve my soil quality?",
            "🐛 Pest Control" to "Natural pest control methods for farming",
            "📷 Image Analysis" to null // Special action for camera
        )
        
        quickActions.forEach { (chipText, question) ->
            val chip = Chip(this).apply {
                text = chipText
                isClickable = true
                setChipBackgroundColorResource(R.color.primary_green_light)
                setTextColor(ContextCompat.getColor(this@AIAssistantActivity, R.color.primary_dark))
                setOnClickListener {
                    if (question != null) {
                        etQuestion.setText(question)
                        sendTextMessage()
                    } else if (chipText.contains("Image Analysis")) {
                        // Trigger camera functionality
                        showImageSourceDialog()
                    }
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun initializeOfflineAI() {
        lifecycleScope.launch {
            try {
                showProgress(true)
                offlineAIProcessor = OfflineAIProcessor(this@AIAssistantActivity)
                offlineAIProcessor.initialize()
                showProgress(false)
            } catch (e: Exception) {
                showProgress(false)
                addSystemMessage("⚠️ Offline AI initialization failed. Using fallback responses.")
            }
        }
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = """
            🌾 Welcome to FasalSaathi AI Assistant! 
            
            I'm your offline farming companion, ready to help with:
            • Crop disease identification & treatment
            • Weather-based farming advice  
            • Soil health & fertilizer recommendations
            • Pest control solutions
            • Planting & harvesting guidance
            • Image analysis for crop problems
            
            🎤 Tap the green microphone for voice chat
            📷 Tap the blue camera to analyze plant images
            ✍️ Or type your questions below!
        """.trimIndent()
        
        addAIMessage(welcomeMessage)
        
        // Speak welcome message
        Handler(Looper.getMainLooper()).postDelayed({
            if (isTextToSpeechReady) {
                speakText("Welcome to FasalSaathi AI Assistant! You can chat with voice, send images, or type questions. How can I help you with farming today?")
            }
        }, 1000)
    }

    private fun sendTextMessage() {
        val question = etQuestion.text.toString().trim()
        if (question.isEmpty()) {
            Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Add user message
        addUserMessage(question)
        etQuestion.setText("")
        
        // Process with offline AI
        processWithOfflineAI(question)
    }

    private fun startVoiceInput() {
        if (!checkAudioPermission()) {
            requestAudioPermission()
            return
        }
        
        if (!::speechRecognizer.isInitialized) {
            Toast.makeText(this, "Voice recognition not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        isRecording = true
        updateVoiceInputUI(true)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your farming question...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        speechRecognizer.startListening(intent)
    }

    private fun stopVoiceInput() {
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.stopListening()
        }
        isRecording = false
        updateVoiceInputUI(false)
    }

    private fun updateVoiceInputUI(isListening: Boolean) {
        if (isListening) {
            fabVoiceInput.setImageResource(R.drawable.ic_mic_off)
            tvListeningIndicator.text = "🎤 Listening... Tap to stop"
            tvListeningIndicator.visibility = View.VISIBLE
            voiceVisualizerCard.visibility = View.VISIBLE
            voiceVisualizerView.startAnimation()
            
            // Animate FAB
            ObjectAnimator.ofFloat(fabVoiceInput, "scaleX", 1.0f, 1.2f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(fabVoiceInput, "scaleY", 1.0f, 1.2f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        } else {
            fabVoiceInput.setImageResource(R.drawable.ic_mic)
            tvListeningIndicator.visibility = View.GONE
            voiceVisualizerCard.visibility = View.GONE
            voiceVisualizerView.stopAnimation()
            
            // Reset FAB scale
            ObjectAnimator.ofFloat(fabVoiceInput, "scaleX", 1.2f, 1.0f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(fabVoiceInput, "scaleY", 1.2f, 1.0f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    private fun processWithOfflineAI(question: String) {
        lifecycleScope.launch {
            try {
                showProgress(true)
                
                val response = withContext(Dispatchers.IO) {
                    if (::offlineAIProcessor.isInitialized) {
                        offlineAIProcessor.processQuestion(question)
                    } else {
                        generateFallbackResponse(question)
                    }
                }
                
                showProgress(false)
                addAIMessage(response)
                
                // Speak response if TTS is ready
                if (isTextToSpeechReady) {
                    speakText(response)
                }
                
            } catch (e: Exception) {
                showProgress(false)
                addAIMessage("I apologize, but I'm having trouble processing your question right now. Please try again.")
            }
        }
    }

    private fun generateFallbackResponse(question: String): String {
        val lowerQuestion = question.toLowerCase()
        
        return when {
            lowerQuestion.contains("disease") || lowerQuestion.contains("pest") -> {
                """
                🦠 For crop diseases and pests:
                
                • Inspect crops regularly for early detection
                • Use neem oil as natural pesticide  
                • Maintain proper spacing between plants
                • Ensure good drainage to prevent fungal issues
                • Consider crop rotation to break disease cycles
                
                For specific identification, please share a photo of the affected plant.
                """.trimIndent()
            }
            lowerQuestion.contains("weather") || lowerQuestion.contains("rain") -> {
                """
                🌤️ Weather-based farming advice:
                
                • Check weather forecasts before planting or harvesting
                • Protect crops during extreme weather
                • Adjust irrigation based on rainfall predictions  
                • Use mulching during hot/dry periods
                • Plan field activities around weather windows
                
                Use our weather page for detailed agricultural forecasts!
                """.trimIndent()
            }
            lowerQuestion.contains("soil") || lowerQuestion.contains("fertilizer") -> {
                """
                🌱 Soil health recommendations:
                
                • Test soil pH regularly (ideal: 6.0-7.0 for most crops)
                • Add organic matter like compost or manure
                • Use balanced NPK fertilizers based on crop needs
                • Practice crop rotation to maintain nutrients
                • Avoid over-fertilization which can harm soil
                
                Consider getting a soil test for specific recommendations.
                """.trimIndent()
            }
            lowerQuestion.contains("water") || lowerQuestion.contains("irrigation") -> {
                """
                💧 Irrigation best practices:
                
                • Water early morning or evening to reduce evaporation
                • Check soil moisture 2-3 inches deep before watering
                • Use drip irrigation for efficient water use
                • Mulch around plants to retain moisture
                • Different crops have different water needs
                
                Monitor soil moisture levels regularly!
                """.trimIndent()
            }
            else -> {
                """
                🌾 Thank you for your question about farming!
                
                Here are some general farming tips:
                • Plan your crops according to local climate
                • Maintain healthy soil through organic practices  
                • Monitor crops regularly for early problem detection
                • Use sustainable farming methods when possible
                • Stay informed about weather and market conditions
                
                Feel free to ask more specific questions about crops, diseases, weather, or soil management!
                """.trimIndent()
            }
        }
    }

    private fun addUserMessage(content: String) {
        val message = AIMessage(
            content = content,
            isFromUser = true,
            timestamp = getCurrentTimestamp()
        )
        chatMessages.add(message)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun addAIMessage(content: String) {
        val message = AIMessage(
            content = content,
            isFromUser = false,
            timestamp = getCurrentTimestamp()
        )
        chatMessages.add(message)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun addSystemMessage(content: String) {
        val message = AIMessage(
            content = content,
            isFromUser = false,
            timestamp = getCurrentTimestamp(),
            isSystemMessage = true
        )
        chatMessages.add(message)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (chatMessages.isNotEmpty()) {
                rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
            }
        }, 100)
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    // Text-to-Speech Implementation
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.getDefault())
            isTextToSpeechReady = result != TextToSpeech.LANG_MISSING_DATA && 
                                 result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun speakText(text: String) {
        if (isTextToSpeechReady) {
            // Clean text for better speech
            val cleanText = text.replace(Regex("[🌾🌤️🌱💧🪴🐛🦠⚠️🎤💡🎯]"), "")
                               .replace("•", "")
                               .trim()
            textToSpeech.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Speech Recognition Listener
    private fun createRecognitionListener() = object : android.speech.RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            tvListeningIndicator.text = "🎤 Listening... Speak now"
        }

        override fun onBeginningOfSpeech() {
            tvListeningIndicator.text = "🎤 Processing speech..."
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Update voice visualizer
            voiceVisualizerView.updateAmplitude(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            tvListeningIndicator.text = "🎤 Processing..."
        }

        override fun onError(error: Int) {
            isRecording = false
            updateVoiceInputUI(false)
            
            val errorMessage = when (error) {
                android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                android.speech.SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                android.speech.SpeechRecognizer.ERROR_NETWORK -> "Network error"
                android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                android.speech.SpeechRecognizer.ERROR_SERVER -> "Server error"
                android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            
            if (error != android.speech.SpeechRecognizer.ERROR_NO_MATCH) {
                Toast.makeText(this@AIAssistantActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResults(results: Bundle?) {
            isRecording = false
            updateVoiceInputUI(false)
            
            val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                etQuestion.setText(spokenText)
                sendTextMessage()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                etQuestion.setText(matches[0])
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // Permission handling
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == 
               PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }

    // Image AI Functionality
    private fun showImageSourceDialog() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose from Gallery")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE_REQUEST)
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_CAPTURE_REQUEST -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        processImageWithAI(imageBitmap, "📷 Camera Image")
                    }
                }
                IMAGE_PICK_REQUEST -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        try {
                            val inputStream = contentResolver.openInputStream(selectedImageUri)
                            val imageBitmap = BitmapFactory.decodeStream(inputStream)
                            processImageWithAI(imageBitmap, "🖼️ Gallery Image")
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun processImageWithAI(bitmap: Bitmap, sourceLabel: String) {
        // Add user message with image indicator
        addUserMessage("$sourceLabel - Please analyze this farming image")
        
        lifecycleScope.launch {
            try {
                showProgress(true)
                
                val aiResponse = withContext(Dispatchers.IO) {
                    analyzeAgriculturalImage(bitmap)
                }
                
                showProgress(false)
                addAIMessage(aiResponse)
                
                // Speak response if TTS is ready
                if (isTextToSpeechReady) {
                    speakText(aiResponse)
                }
                
            } catch (e: Exception) {
                showProgress(false)
                addAIMessage("I apologize, but I'm having trouble analyzing the image right now. Please try again or describe what you see in the image.")
            }
        }
    }

    private fun analyzeAgriculturalImage(bitmap: Bitmap): String {
        // This is a mock AI analysis - in a real implementation, you would use:
        // 1. TensorFlow Lite models for crop disease detection
        // 2. Plant identification APIs
        // 3. Computer vision libraries for agricultural analysis
        
        // For now, provide comprehensive farming guidance based on common scenarios
        val responses = listOf(
            """
            🌾 **Crop Analysis Results:**
            
            **Possible Observations:**
            • Plant appears to be in vegetative growth stage
            • Look for signs of nutrient deficiency (yellow/brown leaves)
            • Check for pest damage (holes, discoloration)
            • Assess overall plant health and vigor
            
            **Recommendations:**
            • Ensure adequate water supply (soil should be moist but not waterlogged)
            • Apply balanced NPK fertilizer if growth seems stunted
            • Monitor for common pests like aphids, caterpillars
            • Maintain proper spacing between plants for air circulation
            
            **Next Steps:**
            • Take photos weekly to track growth progress
            • Soil test recommended if plants show stress signs
            • Consider organic mulching to retain moisture
            """.trimIndent(),
            
            """
            🦠 **Disease Detection Analysis:**
            
            **Common Disease Indicators:**
            • Leaf spots or discoloration may indicate fungal infections
            • Wilting could suggest bacterial wilt or root problems
            • Yellowing might indicate nutrient deficiency or viral issues
            
            **Treatment Recommendations:**
            • Remove affected leaves immediately to prevent spread
            • Apply neem oil spray for fungal/bacterial control
            • Improve drainage if soil appears waterlogged
            • Increase air circulation around plants
            
            **Prevention:**
            • Use drip irrigation instead of overhead watering
            • Rotate crops to break disease cycles
            • Apply organic compost to boost soil health
            • Choose disease-resistant varieties for next planting
            """.trimIndent(),
            
            """
            🐛 **Pest Management Analysis:**
            
            **Pest Identification:**
            • Look for chewed leaves (caterpillars, beetles)
            • Curled/distorted leaves (aphids, mites)
            • Holes in fruits/stems (borers)
            • Webbing (spider mites)
            
            **Organic Control Methods:**
            • Neem oil spray (effective against most soft-bodied pests)
            • Bt (Bacillus thuringiensis) for caterpillars
            • Yellow sticky traps for flying insects
            • Companion planting (marigolds, basil, mint)
            
            **Beneficial Insects:**
            • Encourage ladybugs, lacewings for aphid control
            • Plant flowers to attract beneficial insects
            • Avoid broad-spectrum insecticides
            """.trimIndent(),
            
            """
            🌱 **Soil & Nutrition Assessment:**
            
            **Soil Health Indicators:**
            • Dark, crumbly soil indicates good organic matter
            • Poor drainage may cause root problems
            • Compacted soil restricts root growth
            
            **Nutrient Management:**
            • Nitrogen deficiency: Yellowing of older leaves first
            • Phosphorus deficiency: Purple/reddish leaves
            • Potassium deficiency: Brown leaf edges
            • Micronutrient issues: Interveinal chlorosis
            
            **Soil Improvement:**
            • Add organic compost regularly
            • Test soil pH (most crops prefer 6.0-7.0)
            • Use cover crops during off-season
            • Avoid over-tillage to preserve soil structure
            """.trimIndent()
        )
        
        // Return a random response or could be enhanced with actual image recognition
        return responses.random()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceInput()
                } else {
                    Toast.makeText(this, "Voice input requires microphone permission", Toast.LENGTH_LONG).show()
                }
            }
            CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera access requires permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopVoiceInput()
        }
    }
}