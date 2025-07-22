package com.nighteyecare.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nighteyecare.app.data.entities.AppPreferences
import com.nighteyecare.app.data.entities.FilterSettings

@Database(entities = [FilterSettings::class, AppPreferences::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun filterSettingsDao(): FilterSettingsDao
    abstract fun appPreferencesDao(): AppPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "night_eye_care_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
