package com.nighteyecare.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferences(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val selectedLanguage: String,
    val onboardingCompleted: Boolean,
    val batteryOptimizationShown: Boolean,
    val notificationsEnabled: Boolean = true,
    val isFilterActive: Boolean = false // New field with default true
)