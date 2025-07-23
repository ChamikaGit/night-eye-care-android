package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.launch

class LanguageSelectionViewModel(application: Application, private val appPreferencesManager: AppPreferencesManager) : AndroidViewModel(application) {

    private val _selectedLanguageCode = MutableLiveData<String>()
    val selectedLanguageCode: LiveData<String> = _selectedLanguageCode

    init {
        loadSelectedLanguage()
    }

    private fun loadSelectedLanguage() {
        viewModelScope.launch {
            _selectedLanguageCode.value = appPreferencesManager.getAppPreferences()?.selectedLanguage
        }
    }

    fun setSelectedLanguage(languageCode: String) {
        viewModelScope.launch {
            val currentPrefs = appPreferencesManager.getAppPreferences() ?: return@launch
            appPreferencesManager.saveAppPreferences(currentPrefs.copy(selectedLanguage = languageCode))
            _selectedLanguageCode.value = languageCode // Update LiveData immediately
            // TODO: Implement actual language change and activity recreation in the Activity/Fragment
        }
    }
}