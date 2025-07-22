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
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: FilterSettingsRepository, private val alarmScheduler: AlarmScheduler) : AndroidViewModel(application) {

    private val _filterSettings = MutableLiveData<FilterSettings>()
    val filterSettings: LiveData<FilterSettings> = _filterSettings

    init {
        viewModelScope.launch {
            _filterSettings.value = repository.getFilterSettings() ?: FilterSettings(
                isEnabled = false,
                selectedPreset = "Night Mode",
                intensity = 50,
                dimLevel = 0,
                scheduleEnabled = false,
                scheduleStartTime = "22:00",
                scheduleEndTime = "06:00"
            )
        }
    }

    fun setFilterEnabled(isEnabled: Boolean) {
        val newSettings = _filterSettings.value?.copy(isEnabled = isEnabled)
        newSettings?.let { updateAndSave(it) }
    }

    fun setIntensity(intensity: Int) {
        val newSettings = _filterSettings.value?.copy(intensity = intensity)
        newSettings?.let { updateAndSave(it) }
    }

    fun setDimLevel(dimLevel: Int) {
        val newSettings = _filterSettings.value?.copy(dimLevel = dimLevel)
        newSettings?.let { updateAndSave(it) }
    }

    fun setSelectedPreset(preset: String) {
        val newSettings = _filterSettings.value?.copy(selectedPreset = preset)
        newSettings?.let { updateAndSave(it) }
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

        alarmScheduler.scheduleFilter(startHour, startMinute, "com.nighteyecare.app.ACTION_START_FILTER")
        alarmScheduler.scheduleFilter(endHour, endMinute, "com.nighteyecare.app.ACTION_STOP_FILTER")
    }

    private fun cancelAlarms() {
        alarmScheduler.cancelFilter("com.nighteyecare.app.ACTION_START_FILTER")
        alarmScheduler.cancelFilter("com.nighteyecare.app.ACTION_STOP_FILTER")
    }

    private fun updateAndSave(newSettings: FilterSettings) {
        _filterSettings.value = newSettings
        viewModelScope.launch {
            repository.insertOrUpdate(newSettings)
        }
    }
}
