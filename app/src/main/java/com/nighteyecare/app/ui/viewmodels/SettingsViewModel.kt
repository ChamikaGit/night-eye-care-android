package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nighteyecare.app.data.entities.AppPreferences
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application, private val appPreferencesManager: AppPreferencesManager) : AndroidViewModel(application) {

    private val _appPreferences = MutableLiveData<AppPreferences>()
    val appPreferences: LiveData<AppPreferences> = _appPreferences

    init {
        loadAppPreferences()
    }

    fun loadAppPreferences() {
        viewModelScope.launch {
            _appPreferences.value = appPreferencesManager.getAppPreferences()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesManager.setNotificationsEnabled(enabled)
            loadAppPreferences() // Reload preferences to update UI
        }
    }

    // Add other settings functions here as needed (e.g., setLanguage, setBatteryOptimizationShown)
}