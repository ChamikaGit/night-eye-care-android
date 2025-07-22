package com.nighteyecare.app.data.repository

import com.nighteyecare.app.data.database.FilterSettingsDao
import com.nighteyecare.app.data.entities.FilterSettings

class FilterSettingsRepository(private val filterSettingsDao: FilterSettingsDao) {

    suspend fun getFilterSettings(): FilterSettings? {
        return filterSettingsDao.getFilterSettings()
    }

    suspend fun insertOrUpdate(filterSettings: FilterSettings) {
        filterSettingsDao.insertOrUpdate(filterSettings)
    }
}
