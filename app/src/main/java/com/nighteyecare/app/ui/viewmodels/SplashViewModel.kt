package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(application: Application, private val appPreferencesManager: AppPreferencesManager) : AndroidViewModel(application) {

    private val _navigateTo = MutableLiveData<Class<*>>()
    val navigateTo: LiveData<Class<*>> = _navigateTo

    fun checkOnboardingStatus() {
        viewModelScope.launch {
            delay(2000) // 2 second splash screen
            val onboardingCompleted = appPreferencesManager.isOnboardingCompleted()
            if (onboardingCompleted) {
                _navigateTo.value = com.nighteyecare.app.ui.activities.MainActivity::class.java
            } else {
                _navigateTo.value = com.nighteyecare.app.ui.activities.TutorialActivity::class.java
            }
        }
    }
}