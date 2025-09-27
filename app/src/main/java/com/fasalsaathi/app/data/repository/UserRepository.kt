package com.fasalsaathi.app.data.repository

import android.content.SharedPreferences
import com.fasalsaathi.app.data.local.dao.UserDao
import com.fasalsaathi.app.data.local.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class UserRepository(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) {
    
    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email)
        }
    }
    
    suspend fun getUserByPhone(phoneNumber: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByPhone(phoneNumber)
        }
    }
    
    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }
    
    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.updateUser(user.copy(updatedAt = Date()))
        }
    }
    
    suspend fun deleteUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.deleteUser(user)
        }
    }
    
    suspend fun updateLanguagePreference(userId: String, language: String) {
        withContext(Dispatchers.IO) {
            userDao.updateLanguagePreference(userId, language)
        }
    }
    
    suspend fun updateProfileComplete(userId: String, isComplete: Boolean) {
        withContext(Dispatchers.IO) {
            userDao.updateProfileComplete(userId, isComplete)
        }
    }
    
    fun saveUserSession(user: User) {
        sharedPreferences.edit()
            .putString("current_user_id", user.id)
            .putString("user_email", user.email)
            .putString("user_name", user.fullName)
            .putBoolean("is_logged_in", true)
            .putBoolean("is_profile_complete", user.isProfileComplete)
            .apply()
    }
    
    fun clearUserSession() {
        sharedPreferences.edit()
            .remove("current_user_id")
            .remove("user_email")
            .remove("user_name")
            .putBoolean("is_logged_in", false)
            .putBoolean("is_profile_complete", false)
            .apply()
    }
    
    fun getCurrentUserId(): String? {
        return sharedPreferences.getString("current_user_id", null)
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    fun isProfileComplete(): Boolean {
        return sharedPreferences.getBoolean("is_profile_complete", false)
    }
}