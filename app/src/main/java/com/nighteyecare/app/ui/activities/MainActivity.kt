package com.nighteyecare.app.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.nighteyecare.app.data.database.AppDatabase
import com.nighteyecare.app.data.repository.FilterSettingsRepository
import com.nighteyecare.app.databinding.ActivityMainBinding
import com.nighteyecare.app.services.FilterService
import com.nighteyecare.app.ui.viewmodels.MainViewModel
import com.nighteyecare.app.ui.viewmodels.MainViewModelFactory
import com.nighteyecare.app.ui.adapters.PresetAdapter
import com.nighteyecare.app.ui.fragments.TimePickerFragment
import com.nighteyecare.app.utils.AlarmScheduler
import com.nighteyecare.app.ui.utils.CustomToolbar
import android.widget.ImageView
import com.nighteyecare.app.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var customToolbar: CustomToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customToolbar = CustomToolbar(binding.customToolbar.root)
        customToolbar.setTitle(getString(R.string.app_name))
        customToolbar.showBackButton(false) // Main screen doesn't need a back button

        // Add settings icon to the right of the toolbar
        val settingsIcon = ImageView(this)
        settingsIcon.setImageResource(android.R.drawable.ic_menu_manage)
        settingsIcon.contentDescription = getString(R.string.settings_button_description)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        customToolbar.getRightIconsContainer().addView(settingsIcon)

        val database = AppDatabase.getDatabase(this)
        val repository = FilterSettingsRepository(database.filterSettingsDao())
        val alarmScheduler = AlarmScheduler(this)
        val factory = MainViewModelFactory(application, repository, alarmScheduler)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        setupObservers()
        setupClickListeners()
        setupPresetsRecyclerView()
    }

    private fun setupPresetsRecyclerView() {
        val presets = listOf(
            getString(R.string.candle_preset_name),
            getString(R.string.sunset_preset_name),
            getString(R.string.lamp_preset_name),
            getString(R.string.night_mode_preset_name),
            getString(R.string.room_light_preset_name),
            getString(R.string.sun_preset_name)
        )
        val adapter = PresetAdapter(presets) { preset ->
            viewModel.setSelectedPreset(preset)
        }
        binding.presetsRecyclerview.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.filterSettings.observe(this) { settings ->
            binding.mainToggleButton.text = if (settings.isEnabled) getString(R.string.disable_filter) else getString(R.string.enable_filter)
            binding.statusIndicator.text = if (settings.isEnabled) getString(R.string.filter_enabled) else getString(R.string.filter_disabled)
            binding.intensitySeekbar.progress = settings.intensity
            binding.screenDimSeekbar.progress = settings.dimLevel
            binding.scheduleSwitch.isChecked = settings.scheduleEnabled
            binding.startTimeValue.text = settings.scheduleStartTime
            binding.endTimeValue.text = settings.scheduleEndTime

            if (settings.isEnabled) {
                checkAndStartFilterService()
            } else {
                stopFilterService()
            }
        }
    }

    private fun checkAndStartFilterService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            startFilterService()
        }
    }

    private fun startFilterService() {
        val intent = Intent(this, FilterService::class.java)
        val settings = viewModel.filterSettings.value
        if (settings != null) {
            intent.putExtra("color", getColorForPreset(settings.selectedPreset))
            intent.putExtra("alpha", (settings.intensity * 2.55).toInt())
        }
        startService(intent)
    }

    private fun getColorForPreset(preset: String): Int {
        return when (preset) {
            getString(R.string.candle_preset_name) -> 0xFFFF8C00.toInt()
            getString(R.string.sunset_preset_name) -> 0xFFFF7F50.toInt()
            getString(R.string.lamp_preset_name) -> 0xFFFFD700.toInt()
            getString(R.string.night_mode_preset_name) -> 0xFFFFA500.toInt()
            getString(R.string.room_light_preset_name) -> 0xFFFFFF99.toInt()
            getString(R.string.sun_preset_name) -> 0xFFFFFFFF.toInt()
            else -> 0xFFFFFFFF.toInt()
        }
    }

    private fun stopFilterService() {
        val intent = Intent(this, FilterService::class.java)
        stopService(intent)
    }

    private fun setupClickListeners() {
        binding.mainToggleButton.setOnClickListener {
            viewModel.setFilterEnabled(!viewModel.filterSettings.value!!.isEnabled)
        }

        binding.intensitySeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setIntensity(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.screenDimSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setDimLevel(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.scheduleSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setScheduleEnabled(isChecked)
        }

        binding.startTimeValue.setOnClickListener {
            TimePickerFragment { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                viewModel.setScheduleStartTime(time)
            }.show(supportFragmentManager, "startTimePicker")
        }

        binding.endTimeValue.setOnClickListener {
            TimePickerFragment { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                viewModel.setScheduleEndTime(time)
            }.show(supportFragmentManager, "endTimePicker")
        }

        binding.infoButton.setOnClickListener {
            val intent = Intent(this, PresetInfoActivity::class.java)
            startActivity(intent)
        }
    }
}