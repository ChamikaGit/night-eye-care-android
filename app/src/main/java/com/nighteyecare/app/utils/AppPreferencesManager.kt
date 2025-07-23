package com.nighteyecare.app.utils

import com.nighteyecare.app.R
import android.content.Context
import com.nighteyecare.app.data.database.AppDatabase
import com.nighteyecare.app.data.entities.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppPreferencesManager(private val context: Context) {

    private val appPreferencesDao = AppDatabase.getDatabase(context).appPreferencesDao()

    suspend fun getAppPreferences(): AppPreferences? {
        return appPreferencesDao.getAppPreferences()
    }

    suspend fun saveAppPreferences(preferences: AppPreferences) {
        appPreferencesDao.insertOrUpdate(preferences)
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return getAppPreferences()?.onboardingCompleted ?: false
    }

    private suspend fun getOrCreateAppPreferences(): AppPreferences {
        return getAppPreferences() ?: AppPreferences(
            selectedLanguage = context.getString(R.string.default_language_code),
            onboardingCompleted = false,
            batteryOptimizationShown = false,
            notificationsEnabled = true,
            isFilterActive = false
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        val currentPreferences = getOrCreateAppPreferences()
        saveAppPreferences(currentPreferences.copy(onboardingCompleted = completed))
    }

    suspend fun getNotificationsEnabled(): Boolean {
        return getAppPreferences()?.notificationsEnabled ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        val currentPreferences = getOrCreateAppPreferences()
        saveAppPreferences(currentPreferences.copy(notificationsEnabled = enabled))
    }

    suspend fun getFilterActive(): Boolean {
        return getAppPreferences()?.isFilterActive ?: false
    }

    suspend fun setFilterActive(active: Boolean) {
        val currentPreferences = getOrCreateAppPreferences()
        saveAppPreferences(currentPreferences.copy(isFilterActive = active))
    }
}
