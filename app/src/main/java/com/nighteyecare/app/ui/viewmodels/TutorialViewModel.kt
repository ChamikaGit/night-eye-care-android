package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.launch

class TutorialViewModel(application: Application, private val appPreferencesManager: AppPreferencesManager) : AndroidViewModel(application) {

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            appPreferencesManager.setOnboardingCompleted(true)
        }
    }
}