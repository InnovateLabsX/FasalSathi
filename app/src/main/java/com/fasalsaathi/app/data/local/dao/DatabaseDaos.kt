package com.fasalsaathi.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fasalsaathi.app.data.local.entities.*
import java.util.Date

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhone(phoneNumber: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("UPDATE users SET languagePreference = :language WHERE id = :userId")
    suspend fun updateLanguagePreference(userId: String, language: String)
    
    @Query("UPDATE users SET isProfileComplete = :isComplete WHERE id = :userId")
    suspend fun updateProfileComplete(userId: String, isComplete: Boolean)
}

@Dao
interface CropRecommendationDao {
    @Query("SELECT * FROM crop_recommendations WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRecommendationsByUserId(userId: String): LiveData<List<CropRecommendation>>
    
    @Query("SELECT * FROM crop_recommendations WHERE id = :id")
    suspend fun getRecommendationById(id: String): CropRecommendation?
    
    @Query("SELECT * FROM crop_recommendations WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentRecommendations(userId: String, limit: Int): List<CropRecommendation>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: CropRecommendation)
    
    @Delete
    suspend fun deleteRecommendation(recommendation: CropRecommendation)
    
    @Query("DELETE FROM crop_recommendations WHERE userId = :userId AND createdAt < :date")
    suspend fun deleteOldRecommendations(userId: String, date: Date)
}

@Dao
interface DiseaseDetectionDao {
    @Query("SELECT * FROM disease_detections WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDetectionsByUserId(userId: String): LiveData<List<DiseaseDetection>>
    
    @Query("SELECT * FROM disease_detections WHERE id = :id")
    suspend fun getDetectionById(id: String): DiseaseDetection?
    
    @Query("SELECT * FROM disease_detections WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentDetections(userId: String, limit: Int): List<DiseaseDetection>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DiseaseDetection)
    
    @Delete
    suspend fun deleteDetection(detection: DiseaseDetection)
    
    @Query("DELETE FROM disease_detections WHERE userId = :userId AND createdAt < :date")
    suspend fun deleteOldDetections(userId: String, date: Date)
}

@Dao
interface MarketPriceDao {
    @Query("SELECT * FROM market_prices WHERE cropName = :cropName AND state = :state ORDER BY date DESC LIMIT 1")
    suspend fun getLatestPriceForCrop(cropName: String, state: String): MarketPrice?
    
    @Query("SELECT * FROM market_prices WHERE cropName IN (:cropNames) AND state = :state ORDER BY date DESC")
    suspend fun getPricesForCrops(cropNames: List<String>, state: String): List<MarketPrice>
    
    @Query("SELECT DISTINCT cropName FROM market_prices WHERE state = :state")
    suspend fun getAvailableCrops(state: String): List<String>
    
    @Query("SELECT DISTINCT marketName FROM market_prices WHERE state = :state")
    suspend fun getAvailableMarkets(state: String): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<MarketPrice>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: MarketPrice)
    
    @Query("DELETE FROM market_prices WHERE date < :date")
    suspend fun deleteOldPrices(date: Date)
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_data WHERE location = :location ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getWeatherByLocation(location: String): WeatherData?
    
    @Query("SELECT * FROM weather_data WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getWeatherByCoordinates(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): WeatherData?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherData: WeatherData)
    
    @Query("DELETE FROM weather_data WHERE updatedAt < :date")
    suspend fun deleteOldWeatherData(date: Date)
}

@Dao
interface FarmDataDao {
    @Query("SELECT * FROM farm_data WHERE userId = :userId")
    fun getFarmsByUserId(userId: String): LiveData<List<FarmData>>
    
    @Query("SELECT * FROM farm_data WHERE id = :farmId")
    suspend fun getFarmById(farmId: String): FarmData?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farmData: FarmData)
    
    @Update
    suspend fun updateFarm(farmData: FarmData)
    
    @Delete
    suspend fun deleteFarm(farmData: FarmData)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUserId(userId: String): LiveData<List<Notification>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(userId: String): LiveData<List<Notification>>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): LiveData<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
    
    @Delete
    suspend fun deleteNotification(notification: Notification)
    
    @Query("DELETE FROM notifications WHERE userId = :userId AND createdAt < :date")
    suspend fun deleteOldNotifications(userId: String, date: Date)
}