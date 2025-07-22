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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        val defaultLanguage = context.getString(R.string.default_language_code)
        val currentPreferences = getAppPreferences() ?: AppPreferences(
            selectedLanguage = defaultLanguage,
            onboardingCompleted = false,
            batteryOptimizationShown = false,
            notificationsEnabled = true
        )
        saveAppPreferences(currentPreferences.copy(onboardingCompleted = completed))
    }

    suspend fun getNotificationsEnabled(): Boolean {
        return getAppPreferences()?.notificationsEnabled ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        val defaultLanguage = context.getString(R.string.default_language_code)
        val currentPreferences = getAppPreferences() ?: AppPreferences(
            selectedLanguage = defaultLanguage,
            onboardingCompleted = false,
            batteryOptimizationShown = false,
            notificationsEnabled = true
        )
        saveAppPreferences(currentPreferences.copy(notificationsEnabled = enabled))
    }
}
