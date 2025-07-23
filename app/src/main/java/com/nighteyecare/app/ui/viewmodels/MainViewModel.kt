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

class MainViewModel(application: Application, private val repository: FilterSettingsRepository, private val alarmScheduler: AlarmScheduler, private val filterManager: FilterManager) : AndroidViewModel(application) {

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
        newSettings?.let { settings ->
            updateAndSave(settings)
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

    private fun updateFilterService(settings: FilterSettings) {
        if (settings.isEnabled) {
            val combinedAlpha = ((settings.intensity * 2.55) * (1 - (settings.dimLevel / 100.0))).toInt()
            filterManager.startFilterService(getColorForPreset(settings.selectedPreset), combinedAlpha, settings.isEnabled)
        } else {
            filterManager.stopFilterService()
        }
    }

    private fun getColorForPreset(preset: String): Int {
        return when (preset) {
            getApplication<Application>().getString(R.string.candle_preset_name) -> kelvinToRgb(1800)
            getApplication<Application>().getString(R.string.sunset_preset_name) -> kelvinToRgb(2000)
            getApplication<Application>().getString(R.string.lamp_preset_name) -> kelvinToRgb(2700)
            getApplication<Application>().getString(R.string.night_mode_preset_name) -> kelvinToRgb(3200)
            getApplication<Application>().getString(R.string.room_light_preset_name) -> kelvinToRgb(3400)
            getApplication<Application>().getString(R.string.sun_preset_name) -> kelvinToRgb(5000)
            else -> kelvinToRgb(3200) // Default to Night Mode
        }
    }

    private fun kelvinToRgb(kelvin: Int): Int {
        val temp = kelvin / 100.0

        var r: Double
        var g: Double
        var b: Double

        if (temp < 66) {
            r = 255.0
            g = 99.4708025861 * Math.log(temp) - 161.1195681661
            b = if (temp <= 19) {
                0.0
            } else {
                50.5596851305 * Math.log(temp - 10) - 68.1120455596
            }
        } else {
            r = 329.698727446 * Math.pow(temp - 60, -0.1332047592)
            g = 288.1221695283 * Math.pow(temp - 60, -0.0755148492)
            b = 255.0
        }

        return android.graphics.Color.rgb(r.coerceIn(0.0, 255.0).toInt(), g.coerceIn(0.0, 255.0).toInt(), b.coerceIn(0.0, 255.0).toInt())
    }
}
