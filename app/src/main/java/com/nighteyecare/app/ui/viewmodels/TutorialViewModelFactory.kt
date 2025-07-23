package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nighteyecare.app.utils.AppPreferencesManager

class TutorialViewModelFactory(private val application: Application, private val appPreferencesManager: AppPreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TutorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TutorialViewModel(application, appPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}