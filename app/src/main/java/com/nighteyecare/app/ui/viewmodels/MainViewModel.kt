package com.nighteyecare.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nighteyecare.app.R
import com.nighteyecare.app.data.entities.FilterSettings
import com.nighteyecare.app.data.repository.FilterSettingsRepository
import com.nighteyecare.app.utils.AlarmScheduler
import com.nighteyecare.app.utils.FilterManager
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

class MainViewModel(application: Application, private val repository: FilterSettingsRepository, private val alarmScheduler: AlarmScheduler, private val filterManager: FilterManager) : AndroidViewModel(application) {

    private val _filterSettings = MutableLiveData<FilterSettings>()
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    init {
        viewModelScope.launch {
            val settings = repository.getFilterSettings() ?: FilterSettings(
                isEnabled = false,
                selectedPreset = "Night Mode",
                intensity = 50,
                dimLevel = 0,
                scheduleEnabled = false,
                scheduleStartTime = "22:00",
                scheduleEndTime = "06:00"
            )
            _filterSettings.value = settings
            // Start the service only if it was previously enabled
            if (settings.isEnabled) {
                updateFilterService(settings)
            }
        }
    }

    fun setFilterEnabled(isEnabled: Boolean) {
        val newSettings = _filterSettings.value?.copy(isEnabled = isEnabled)
        newSettings?.let { settings ->
            updateAndSave(settings)
            filterManager.updateNotificationState(settings.isEnabled)
            if (settings.isEnabled) {
                val updatedSettings = settings.copy(selectedPreset = getApplication<Application>().getString(R.string.night_mode_preset_name))
                updateAndSave(updatedSettings) // Save again with updated preset
                updateFilterService(updatedSettings)
            } else {
                updateFilterService(settings)
            }
        }
    }

    fun setIntensity(intensity: Int) {
        val newSettings = _filterSettings.value?.copy(intensity = intensity)
        newSettings?.let { settings ->
            updateAndSave(settings)
            updateFilterService(settings)
        }
    }

    fun setDimLevel(dimLevel: Int) {
        val newSettings = _filterSettings.value?.copy(dimLevel = dimLevel)
        newSettings?.let { settings ->
            updateAndSave(settings)
            updateFilterService(settings)
        }
    }

    fun setSelectedPreset(preset: String) {
        val newSettings = _filterSettings.value?.copy(selectedPreset = preset)
        newSettings?.let { settings ->
            updateAndSave(settings)
            updateFilterService(settings)
        }
    }

    fun setScheduleEnabled(isEnabled: Boolean) {
        val newSettings = _filterSettings.value?.copy(scheduleEnabled = isEnabled)
        newSettings?.let { settings ->
            updateAndSave(settings)
            if (settings.scheduleEnabled) {
                scheduleAlarms(settings.scheduleStartTime, settings.scheduleEndTime)
            } else {
                cancelAlarms()
            }
        }
    }

    fun setScheduleStartTime(time: String) {
        val newSettings = _filterSettings.value?.copy(scheduleStartTime = time)
        newSettings?.let { settings ->
            updateAndSave(settings)
            if (settings.scheduleEnabled) {
                scheduleAlarms(settings.scheduleStartTime, settings.scheduleEndTime)
            }
        }
    }

    fun setScheduleEndTime(time: String) {
        val newSettings = _filterSettings.value?.copy(scheduleEndTime = time)
        newSettings?.let { settings ->
            updateAndSave(settings)
            if (settings.scheduleEnabled) {
                scheduleAlarms(settings.scheduleStartTime, settings.scheduleEndTime)
            }
        }
    }

    private fun scheduleAlarms(startTime: String, endTime: String) {
        val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
        val (endHour, endMinute) = endTime.split(":").map { it.toInt() }

        alarmScheduler.scheduleFilter(startHour, startMinute, "com.nighteyecare.app.START_FILTER")
        alarmScheduler.scheduleFilter(endHour, endMinute, "com.nighteyecare.app.STOP_FILTER")
    }

    private fun cancelAlarms() {
        alarmScheduler.cancelFilter("com.nighteyecare.app.START_FILTER")
        alarmScheduler.cancelFilter("com.nighteyecare.app.STOP_FILTER")
    }

    private fun updateAndSave(newSettings: FilterSettings) {
        _filterSettings.value = newSettings
        viewModelScope.launch {
            repository.insertOrUpdate(newSettings)
        }
    }

    private fun updateFilterService(settings: FilterSettings) {
        if (settings.isEnabled) {
            filterManager.startFilterService(getColorForPreset(settings.selectedPreset), (settings.intensity * 2.55).toInt(), settings.dimLevel, settings.isEnabled)
        } else {
            filterManager.stopFilterService()
        }
    }

    private fun getColorForPreset(preset: String): Int {
        return when (preset) {
            getApplication<Application>().getString(R.string.candle_preset_name) -> ContextCompat.getColor(getApplication(), R.color.candle)
            getApplication<Application>().getString(R.string.sunset_preset_name) -> ContextCompat.getColor(getApplication(), R.color.sunset)
            getApplication<Application>().getString(R.string.lamp_preset_name) -> ContextCompat.getColor(getApplication(), R.color.lamp)
            getApplication<Application>().getString(R.string.night_mode_preset_name) -> ContextCompat.getColor(getApplication(), R.color.night_mode)
            getApplication<Application>().getString(R.string.room_light_preset_name) -> ContextCompat.getColor(getApplication(), R.color.room_light)
            getApplication<Application>().getString(R.string.sun_preset_name) -> ContextCompat.getColor(getApplication(), R.color.sun)
            getApplication<Application>().getString(R.string.twilight_preset_name) -> ContextCompat.getColor(getApplication(), R.color.twilight)
            else -> ContextCompat.getColor(getApplication(), R.color.night_mode) // Default to Night Mode
        }
    }

    
}
