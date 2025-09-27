package com.fasalsaathi.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.fasalsaathi.app.data.local.dao.*
import com.fasalsaathi.app.data.local.entities.*
import com.fasalsaathi.app.utils.Converters

@Database(
    entities = [
        User::class,
        CropRecommendation::class,
        DiseaseDetection::class,
        MarketPrice::class,
        WeatherData::class,
        FarmData::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FasalSaathiDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun cropRecommendationDao(): CropRecommendationDao
    abstract fun diseaseDetectionDao(): DiseaseDetectionDao
    abstract fun marketPriceDao(): MarketPriceDao
    abstract fun weatherDao(): WeatherDao
    abstract fun farmDataDao(): FarmDataDao
    abstract fun notificationDao(): NotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: FasalSaathiDatabase? = null
        
        fun getDatabase(context: Context): FasalSaathiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FasalSaathiDatabase::class.java,
                    "fasalsaathi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}