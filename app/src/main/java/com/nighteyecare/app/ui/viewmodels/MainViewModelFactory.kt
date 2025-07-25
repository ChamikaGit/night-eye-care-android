package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nighteyecare.app.data.repository.FilterSettingsRepository
import com.nighteyecare.app.utils.AlarmScheduler

import com.nighteyecare.app.utils.FilterManager

class MainViewModelFactory(private val application: Application, private val repository: FilterSettingsRepository, private val alarmScheduler: AlarmScheduler, private val filterManager: FilterManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository, alarmScheduler, filterManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
