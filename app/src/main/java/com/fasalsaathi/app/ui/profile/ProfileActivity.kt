package com.fasalsaathi.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.fasalsaathi.app.FasalSaathiApplication
import com.fasalsaathi.app.R
import com.fasalsaathi.app.ui.auth.LoginActivity
import com.fasalsaathi.app.ui.base.BaseBottomNavigationActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : BaseBottomNavigationActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var ivProfilePicture: CircleImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserLocation: TextView
    private lateinit var tvFarmSize: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSettings: MaterialButton
    private lateinit var btnLogout: MaterialButton
    
    private lateinit var cardPersonalInfo: MaterialCardView
    private lateinit var cardFarmInfo: MaterialCardView
    private lateinit var cardAppSettings: MaterialCardView
    private lateinit var cardSupport: MaterialCardView
    
    private var profileImageBitmap: Bitmap? = null
    
    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_GALLERY = 101
    }
    
    override fun getCurrentNavItemId(): Int {
        return R.id.nav_profile
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        initializeViews()
        setupToolbar()
        setupBottomNavigation()
        loadUserProfile()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserLocation = findViewById(R.id.tvUserLocation)
        tvFarmSize = findViewById(R.id.tvFarmSize)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnSettings = findViewById(R.id.btnSettings)
        btnLogout = findViewById(R.id.btnLogout)
        
        cardPersonalInfo = findViewById(R.id.cardPersonalInfo)
        cardFarmInfo = findViewById(R.id.cardFarmInfo)
        cardAppSettings = findViewById(R.id.cardAppSettings)
        cardSupport = findViewById(R.id.cardSupport)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "My Profile"
    }
    
    private fun loadUserProfile() {
        val app = application as FasalSaathiApplication
        val sharedPrefs = app.sharedPreferences
        
        // Load user data
        val userName = sharedPrefs.getString("user_name", "Farmer Name") ?: "Farmer Name"
        val userEmail = sharedPrefs.getString("user_email", "farmer@email.com") ?: "farmer@email.com"
        val userPhone = sharedPrefs.getString("user_phone", "Not provided") ?: "Not provided"
        val userState = sharedPrefs.getString("user_state", "Not provided") ?: "Not provided"
        val userCity = sharedPrefs.getString("user_city", "Not provided") ?: "Not provided"
        val farmSize = sharedPrefs.getString("user_farm_size", "Not specified") ?: "Not specified"
        val hasProfilePhoto = sharedPrefs.getBoolean("has_profile_photo", false)
        
        // Update UI
        tvUserName.text = userName
        tvUserEmail.text = userEmail
        tvUserPhone.text = if (userPhone != "Not provided") "+91 $userPhone" else "Phone not provided"
        tvUserLocation.text = if (userCity != "Not provided" && userState != "Not provided") {
            "$userCity, $userState"
        } else {
            "Location not provided"
        }
        tvFarmSize.text = farmSize
        
        // Load profile picture if exists
        if (hasProfilePhoto) {
            // In a real app, you would load the image from storage
            // For now, set a placeholder
            ivProfilePicture.setImageResource(R.drawable.ic_farmer_avatar)
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_farmer_avatar)
        }
    }
    
    private fun setupClickListeners() {
        ivProfilePicture.setOnClickListener {
            showProfilePictureOptions()
        }
        
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        
        cardPersonalInfo.setOnClickListener {
            showPersonalInfoDetails()
        }
        
        cardFarmInfo.setOnClickListener {
            showFarmInfoDetails()
        }
        
        cardAppSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        cardSupport.setOnClickListener {
            showSupportOptions()
        }
    }
    
    private fun showProfilePictureOptions() {
        val options = arrayOf(
            getString(R.string.take_photo),
            getString(R.string.choose_from_gallery),
            "Remove Photo"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> removeProfilePicture()
                }
            }
            .show()
    }
    
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CAMERA)
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }
    
    private fun removeProfilePicture() {
        ivProfilePicture.setImageResource(R.drawable.ic_farmer_avatar)
        profileImageBitmap = null
        
        val app = application as FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        editor.putBoolean("has_profile_photo", false)
        editor.apply()
        
        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        ivProfilePicture.setImageBitmap(it)
                        profileImageBitmap = it
                        saveProfilePicture()
                    }
                }
                REQUEST_GALLERY -> {
                    val imageUri = data?.data
                    imageUri?.let { uri ->
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            ivProfilePicture.setImageBitmap(bitmap)
                            profileImageBitmap = bitmap
                            saveProfilePicture()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun saveProfilePicture() {
        val app = application as FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        editor.putBoolean("has_profile_photo", true)
        editor.apply()
        
        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
    }
    
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.etAddress)
        
        // Pre-fill current data
        val app = application as FasalSaathiApplication
        val sharedPrefs = app.sharedPreferences
        
        etName.setText(sharedPrefs.getString("user_name", ""))
        etPhone.setText(sharedPrefs.getString("user_phone", ""))
        etAddress.setText(sharedPrefs.getString("user_address", ""))
        
        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()
                val newAddress = etAddress.text.toString().trim()
                
                if (newName.isNotEmpty()) {
                    val editor = sharedPrefs.edit()
                    editor.putString("user_name", newName)
                    editor.putString("user_phone", newPhone)
                    editor.putString("user_address", newAddress)
                    editor.apply()
                    
                    loadUserProfile() // Refresh the display
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showPersonalInfoDetails() {
        val app = application as FasalSaathiApplication
        val sharedPrefs = app.sharedPreferences
        
        val message = """
            Name: ${sharedPrefs.getString("user_name", "Not provided")}
            Email: ${sharedPrefs.getString("user_email", "Not provided")}
            Phone: ${sharedPrefs.getString("user_phone", "Not provided")}
            Address: ${sharedPrefs.getString("user_address", "Not provided")}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Personal Information")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showFarmInfoDetails() {
        val app = application as FasalSaathiApplication
        val sharedPrefs = app.sharedPreferences
        
        val message = """
            State: ${sharedPrefs.getString("user_state", "Not provided")}
            City: ${sharedPrefs.getString("user_city", "Not provided")}
            Farm Size: ${sharedPrefs.getString("user_farm_size", "Not specified")}
            Experience: ${sharedPrefs.getString("farming_experience", "Not specified")} years
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Farm Information")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSupportOptions() {
        val options = arrayOf(
            "Help Center",
            "Contact Support",
            "FAQ",
            "App Feedback"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Support & Help")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Opening Help Center...", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Opening Contact Support...", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "Opening FAQ...", Toast.LENGTH_SHORT).show()
                    3 -> showFeedbackDialog()
                }
            }
            .show()
    }
    
    private fun showFeedbackDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
        val etFeedback = dialogView.findViewById<EditText>(R.id.etFeedback)
        
        AlertDialog.Builder(this)
            .setTitle("App Feedback")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val feedback = etFeedback.text.toString().trim()
                if (feedback.isNotEmpty()) {
                    Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from FasalSaathi?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        val app = application as FasalSaathiApplication
        val editor = app.sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.remove("user_email")
        // Keep user data but mark as logged out
        editor.apply()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}