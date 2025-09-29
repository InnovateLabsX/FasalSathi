package com.fasalsaathi.app.ui.ai

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    
    // Constants
    companion object {
        private const val RECORD_AUDIO_PERMISSION = 1001
        private const val VOICE_RECOGNITION_REQUEST = 1002
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
            "🐛 Pest Control" to "Natural pest control methods for farming"
        )
        
        quickActions.forEach { (chipText, question) ->
            val chip = Chip(this).apply {
                text = chipText
                isClickable = true
                setChipBackgroundColorResource(R.color.primary_green_light)
                setTextColor(ContextCompat.getColor(this@AIAssistantActivity, R.color.primary_dark))
                setOnClickListener {
                    etQuestion.setText(question)
                    sendTextMessage()
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
            
            🎤 Tap the microphone for voice chat or type your questions below!
        """.trimIndent()
        
        addAIMessage(welcomeMessage)
        
        // Speak welcome message
        Handler(Looper.getMainLooper()).postDelayed({
            if (isTextToSpeechReady) {
                speakText("Welcome to FasalSaathi AI Assistant! How can I help you with farming today?")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Voice input is now available!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Voice input requires microphone permission", Toast.LENGTH_LONG).show()
                }
            }
        }
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

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopVoiceInput()
        }
    }
}