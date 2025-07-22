package com.nighteyecare.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nighteyecare.app.data.entities.FilterSettings

@Dao
interface FilterSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(filterSettings: FilterSettings)

    @Query("SELECT * FROM filter_settings ORDER BY id DESC LIMIT 1")
    suspend fun getFilterSettings(): FilterSettings?
}
