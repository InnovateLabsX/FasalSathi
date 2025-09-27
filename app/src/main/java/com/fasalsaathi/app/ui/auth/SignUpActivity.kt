package com.fasalsaathi.app.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fasalsaathi.app.R
import com.fasalsaathi.app.FasalSaathiApplication
import com.fasalsaathi.app.data.model.IndianCitiesData
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etAddress: EditText
    private lateinit var etCaptcha: EditText
    private lateinit var spinnerState: Spinner
    private lateinit var spinnerDistrict: Spinner
    private lateinit var spinnerFarmSize: Spinner
    private lateinit var btnSignUp: Button
    private lateinit var btnLogin: Button
    private lateinit var tvCaptcha: TextView
    private lateinit var btnRefreshCaptcha: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var cbTerms: CheckBox
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var btnSelectPhoto: Button
    private lateinit var tvPasswordStrength: TextView
    
    private var captchaAnswer: String = ""
    private var profileImageBitmap: Bitmap? = null
    
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_STORAGE_PERMISSION = 101
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        initViews()
        setupSpinners()
        setupListeners()
        generateNewCaptcha()
    }
    
    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etAddress = findViewById(R.id.etAddress)
        etCaptcha = findViewById(R.id.etCaptcha)
        spinnerState = findViewById(R.id.spinnerState)
        spinnerDistrict = findViewById(R.id.spinnerDistrict)
        spinnerFarmSize = findViewById(R.id.spinnerFarmSize)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        tvCaptcha = findViewById(R.id.tvCaptcha)
        btnRefreshCaptcha = findViewById(R.id.btnRefreshCaptcha)
        progressBar = findViewById(R.id.progressBar)
        cbTerms = findViewById(R.id.cbTerms)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto)
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength)
    }
    
    private fun setupSpinners() {
        // States of India
        val states = arrayOf(
            "Select State", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
            "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
            "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal"
        )
        
        val stateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerState.adapter = stateAdapter
        
        // Farm sizes
        val farmSizes = arrayOf(
            "Select Farm Size", "Less than 1 acre", "1-2 acres", "2-5 acres", 
            "5-10 acres", "10-20 acres", "20+ acres"
        )
        
        val farmSizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, farmSizes)
        farmSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFarmSize.adapter = farmSizeAdapter
        
        // District will be populated based on state selection
        setupDistrictSpinner()
    }
    
    private fun setupDistrictSpinner() {
        spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedState = spinnerState.selectedItem.toString()
                    populateCities(selectedState)
                } else {
                    val cities = arrayOf("Select City First")
                    val cityAdapter = ArrayAdapter(this@SignUpActivity, android.R.layout.simple_spinner_item, cities)
                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerDistrict.adapter = cityAdapter
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun populateCities(state: String) {
        // Get cities from comprehensive Indian cities database
        val citiesInState = IndianCitiesData.getCitiesByState(state)
        val cityNames = mutableListOf<String>()
        cityNames.add("Select City")
        cityNames.addAll(citiesInState.map { it.name })
        
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNames)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrict.adapter = cityAdapter
    }
    
    private fun setupListeners() {
        // Name validation
        etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateName()
                validateForm()
            }
        })
        
        // Email validation
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail()
                validateForm()
            }
        })
        
        // Phone validation
        etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePhone()
                validateForm()
            }
        })
        
        // Password validation
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword()
                checkPasswordStrength()
                validateForm()
            }
        })
        
        // Confirm password validation
        etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateConfirmPassword()
                validateForm()
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
        
        cbTerms.setOnCheckedChangeListener { _, _ -> validateForm() }
        
        btnSignUp.setOnClickListener { performSignUp() }
        btnLogin.setOnClickListener { navigateToLogin() }
        btnRefreshCaptcha.setOnClickListener { generateNewCaptcha() }
        btnSelectPhoto.setOnClickListener { selectProfilePhoto() }
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
    
    private fun validateName(): Boolean {
        val name = etFullName.text.toString().trim()
        return when {
            name.isEmpty() -> {
                etFullName.error = getString(R.string.error_name_required)
                false
            }
            name.length < 2 -> {
                etFullName.error = getString(R.string.error_name_too_short)
                false
            }
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> {
                etFullName.error = getString(R.string.error_name_invalid_characters)
                false
            }
            else -> {
                etFullName.error = null
                true
            }
        }
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
    
    private fun validatePhone(): Boolean {
        val phone = etPhone.text.toString().trim()
        return when {
            phone.isEmpty() -> {
                etPhone.error = getString(R.string.error_phone_required)
                false
            }
            phone.length != 10 -> {
                etPhone.error = getString(R.string.error_phone_invalid_length)
                false
            }
            !phone.matches(Regex("^[0-9]+$")) -> {
                etPhone.error = getString(R.string.error_phone_invalid_characters)
                false
            }
            else -> {
                etPhone.error = null
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
            password.length < 8 -> {
                etPassword.error = getString(R.string.error_password_too_short_signup)
                false
            }
            !password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) -> {
                etPassword.error = getString(R.string.error_password_weak)
                false
            }
            else -> {
                etPassword.error = null
                true
            }
        }
    }
    
    private fun validateConfirmPassword(): Boolean {
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        return when {
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = getString(R.string.error_confirm_password_required)
                false
            }
            password != confirmPassword -> {
                etConfirmPassword.error = getString(R.string.error_passwords_dont_match)
                false
            }
            else -> {
                etConfirmPassword.error = null
                true
            }
        }
    }
    
    private fun validateCaptcha(): Boolean {
        val userAnswer = etCaptcha.text.toString().trim()
        return userAnswer == captchaAnswer
    }
    
    private fun checkPasswordStrength() {
        val password = etPassword.text.toString()
        when {
            password.isEmpty() -> {
                tvPasswordStrength.text = ""
                tvPasswordStrength.visibility = View.GONE
            }
            password.length < 6 -> {
                tvPasswordStrength.text = getString(R.string.password_strength_weak)
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.error_red))
                tvPasswordStrength.visibility = View.VISIBLE
            }
            password.length < 8 || !password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) -> {
                tvPasswordStrength.text = getString(R.string.password_strength_medium)
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.accent_yellow))
                tvPasswordStrength.visibility = View.VISIBLE
            }
            else -> {
                tvPasswordStrength.text = getString(R.string.password_strength_strong)
                tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
                tvPasswordStrength.visibility = View.VISIBLE
            }
        }
    }
    
    private fun validateForm(): Boolean {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPhoneValid = validatePhone()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        val isCaptchaValid = validateCaptcha()
        val isTermsAccepted = cbTerms.isChecked
        val isStateSelected = spinnerState.selectedItemPosition > 0
        val isCitySelected = spinnerDistrict.selectedItemPosition > 0
        val isFarmSizeSelected = spinnerFarmSize.selectedItemPosition > 0
        
        val isValid = isNameValid && isEmailValid && isPhoneValid && 
                isPasswordValid && isConfirmPasswordValid && isCaptchaValid && 
                isTermsAccepted && isStateSelected && isCitySelected && isFarmSizeSelected
        
        btnSignUp.isEnabled = isValid
        return isValid
    }
    
    private fun selectProfilePhoto() {
        val options = arrayOf(
            getString(R.string.take_photo),
            getString(R.string.choose_from_gallery),
            getString(R.string.cancel)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_profile_photo))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }
    
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }
    
    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        profileImageBitmap = it
                        ivProfilePhoto.setImageBitmap(it)
                        btnSelectPhoto.text = getString(R.string.photo_selected)
                    }
                }
                REQUEST_IMAGE_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                            profileImageBitmap = bitmap
                            ivProfilePhoto.setImageBitmap(bitmap)
                            btnSelectPhoto.text = getString(R.string.photo_selected)
                        } catch (e: Exception) {
                            Toast.makeText(this, getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, getString(R.string.storage_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun performSignUp() {
        if (!validateForm()) {
            if (!validateCaptcha()) {
                etCaptcha.error = getString(R.string.error_invalid_captcha)
                generateNewCaptcha()
            }
            return
        }
        
        showLoading(true)
        
        // Simulate sign-up process
        simulateSignUpProcess()
    }
    
    private fun simulateSignUpProcess() {
        // Simulate network delay
        btnSignUp.postDelayed({
            showLoading(false)
            
            // Save user data
            saveUserData()
            
            // Show success message
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.signup_successful))
                .setMessage(getString(R.string.signup_success_message))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    navigateToLogin()
                }
                .setCancelable(false)
                .show()
            
        }, 2000)
    }
    
    private fun saveUserData() {
        val app = application as FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        
        // Save user profile data
        editor.putString("user_name", etFullName.text.toString().trim())
        editor.putString("user_email", etEmail.text.toString().trim())
        editor.putString("user_phone", etPhone.text.toString().trim())
        editor.putString("user_address", etAddress.text.toString().trim())
        editor.putString("user_state", spinnerState.selectedItem.toString())
        
        // Save selected city with coordinates for weather integration
        val selectedState = spinnerState.selectedItem.toString()
        val selectedCityName = spinnerDistrict.selectedItem.toString()
        editor.putString("user_city", selectedCityName)
        
        // Find and save city coordinates for weather API
        val citiesInState = IndianCitiesData.getCitiesByState(selectedState)
        val selectedCity = citiesInState.find { it.name == selectedCityName }
        if (selectedCity != null) {
            editor.putFloat("user_city_lat", selectedCity.latitude.toFloat())
            editor.putFloat("user_city_lon", selectedCity.longitude.toFloat())
        }
        
        editor.putString("user_farm_size", spinnerFarmSize.selectedItem.toString())
        
        // In real app, you would save the profile image to internal storage
        // and save the path here
        if (profileImageBitmap != null) {
            editor.putBoolean("has_profile_photo", true)
        }
        
        editor.apply()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSignUp.isEnabled = !show
        btnLogin.isEnabled = !show
    }
}