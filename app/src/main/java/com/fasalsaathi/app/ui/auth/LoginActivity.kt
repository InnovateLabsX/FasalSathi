package com.fasalsaathi.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.dashboard.DashboardActivity
import com.fasalsaathi.app.FasalSaathiApplication
import java.util.*
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etCaptcha: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvCaptcha: TextView
    private lateinit var btnRefreshCaptcha: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var cbRememberMe: CheckBox
    
    private var captchaAnswer: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        initViews()
        setupListeners()
        generateNewCaptcha()
        loadSavedCredentials()
    }
    
    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etCaptcha = findViewById(R.id.etCaptcha)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvCaptcha = findViewById(R.id.tvCaptcha)
        btnRefreshCaptcha = findViewById(R.id.btnRefreshCaptcha)
        progressBar = findViewById(R.id.progressBar)
        cbRememberMe = findViewById(R.id.cbRememberMe)
    }
    
    private fun setupListeners() {
        // Email validation
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail()
            }
        })
        
        // Password validation
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword()
            }
        })
        
        // CAPTCHA validation
        etCaptcha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        })
        
        btnLogin.setOnClickListener { performLogin() }
        btnSignUp.setOnClickListener { navigateToSignUp() }
        tvForgotPassword.setOnClickListener { handleForgotPassword() }
        btnRefreshCaptcha.setOnClickListener { generateNewCaptcha() }
    }
    
    private fun generateNewCaptcha() {
        val num1 = Random.nextInt(1, 10)
        val num2 = Random.nextInt(1, 10)
        val operators = listOf("+", "-", "*")
        val operator = operators.random()
        
        captchaAnswer = when (operator) {
            "+" -> (num1 + num2).toString()
            "-" -> (num1 - num2).toString()
            "*" -> (num1 * num2).toString()
            else -> "0"
        }
        
        tvCaptcha.text = "$num1 $operator $num2 = ?"
        etCaptcha.text.clear()
        validateForm()
    }
    
    private fun validateEmail(): Boolean {
        val email = etEmail.text.toString().trim()
        return when {
            email.isEmpty() -> {
                etEmail.error = getString(R.string.error_email_required)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = getString(R.string.error_invalid_email)
                false
            }
            else -> {
                etEmail.error = null
                true
            }
        }
    }
    
    private fun validatePassword(): Boolean {
        val password = etPassword.text.toString()
        return when {
            password.isEmpty() -> {
                etPassword.error = getString(R.string.error_password_required)
                false
            }
            password.length < 6 -> {
                etPassword.error = getString(R.string.error_password_too_short)
                false
            }
            else -> {
                etPassword.error = null
                true
            }
        }
    }
    
    private fun validateCaptcha(): Boolean {
        val userAnswer = etCaptcha.text.toString().trim()
        return userAnswer == captchaAnswer
    }
    
    private fun validateForm(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isCaptchaValid = validateCaptcha()
        
        btnLogin.isEnabled = isEmailValid && isPasswordValid && isCaptchaValid
        return isEmailValid && isPasswordValid && isCaptchaValid
    }
    
    private fun performLogin() {
        if (!validateForm()) {
            if (!validateCaptcha()) {
                etCaptcha.error = getString(R.string.error_invalid_captcha)
                generateNewCaptcha()
            }
            return
        }
        
        showLoading(true)
        
        // Simulate login process
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        
        // In real app, this would be an API call
        simulateLoginProcess(email, password)
    }
    
    private fun simulateLoginProcess(email: String, password: String) {
        // Simulate network delay
        btnLogin.postDelayed({
            showLoading(false)
            
            // For demo purposes, accept any valid email/password combination
            if (email.isNotEmpty() && password.length >= 6) {
                // Save login state
                saveLoginState(email)
                
                // Navigate to dashboard
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show()
                generateNewCaptcha()
            }
        }, 2000)
    }
    
    private fun saveLoginState(email: String) {
        val app = application as FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        editor.putBoolean("is_logged_in", true)
        editor.putString("user_email", email)
        
        // Retrieve and save farmer's name from existing stored data
        val existingName = app.sharedPreferences.getString("user_name", "")
        if (existingName.isNullOrEmpty()) {
            // If no name is stored (e.g., first-time login), extract from email
            val farmerName = email.substringBefore("@").replace(".", " ").replace("_", " ")
                .split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            editor.putString("user_name", farmerName)
        }
        
        if (cbRememberMe.isChecked) {
            editor.putString("remembered_email", email)
        } else {
            editor.remove("remembered_email")
        }
        
        editor.apply()
    }
    
    private fun loadSavedCredentials() {
        val app = application as FasalSaathiApplication
        val rememberedEmail = app.sharedPreferences.getString("remembered_email", "")
        if (!rememberedEmail.isNullOrEmpty()) {
            etEmail.setText(rememberedEmail)
            cbRememberMe.isChecked = true
        }
    }
    
    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
    
    private fun handleForgotPassword() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.enter_valid_email_first), Toast.LENGTH_SHORT).show()
            etEmail.requestFocus()
            return
        }
        
        // In real app, this would trigger password reset email
        Toast.makeText(this, getString(R.string.password_reset_sent, email), Toast.LENGTH_LONG).show()
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnSignUp.isEnabled = !show
        etEmail.isEnabled = !show
        etPassword.isEnabled = !show
        etCaptcha.isEnabled = !show
        btnRefreshCaptcha.isEnabled = !show
    }
}