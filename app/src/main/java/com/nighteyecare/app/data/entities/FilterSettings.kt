package com.nighteyecare.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_settings")
data class FilterSettings(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val isEnabled: Boolean,
    val selectedPreset: String,
    val intensity: Int,
    val dimLevel: Int,
    val scheduleEnabled: Boolean,
    val scheduleStartTime: String,
    val scheduleEndTime: String
)
