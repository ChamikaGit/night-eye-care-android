package com.nighteyecare.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nighteyecare.app.data.entities.AppPreferences

@Dao
interface AppPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appPreferences: AppPreferences)

    @Query("SELECT * FROM app_preferences ORDER BY id DESC LIMIT 1")
    suspend fun getAppPreferences(): AppPreferences?
}
