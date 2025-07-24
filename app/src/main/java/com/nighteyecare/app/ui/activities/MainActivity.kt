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
import com.nighteyecare.app.utils.FilterManager
import com.nighteyecare.app.ui.viewmodels.MainViewModel
import com.nighteyecare.app.ui.viewmodels.MainViewModelFactory
import com.nighteyecare.app.ui.adapters.PresetAdapter
import com.nighteyecare.app.ui.fragments.TimePickerFragment
import com.nighteyecare.app.utils.AlarmScheduler
import com.nighteyecare.app.ui.utils.CustomToolbar
import com.nighteyecare.app.ui.utils.CustomAlertDialog
import android.widget.ImageView
import android.content.Context
import android.os.PowerManager
import android.view.View
import com.nighteyecare.app.R
import com.nighteyecare.app.data.model.BlueLightPreset
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var customToolbar: CustomToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial state for the toggle button and status indicator
        binding.mainToggleButton.text = getString(R.string.enable_filter)
        binding.statusIndicator.text = getString(R.string.filter_disabled)

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
        val factory = MainViewModelFactory(application, repository, alarmScheduler, FilterManager(this))
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        setupObservers()
        setupClickListeners()
        setupPresetsRecyclerView()
        setupBatteryOptimizationCard()
    }

    override fun onResume() {
        super.onResume()
        checkBatteryOptimizationStatus()
    }

    private fun setupBatteryOptimizationCard() {
        binding.batteryOptimizationCardButton.setOnClickListener {
            val dialog = CustomAlertDialog(this)
            dialog.showConfirmationDialog(
                title = getString(R.string.battery_optimization_title),
                message = getString(R.string.battery_optimization_message),
                positiveButtonText = getString(R.string.battery_optimization_positive_button),
                negativeButtonText = getString(R.string.battery_optimization_negative_button),
                onPositiveClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.setData(Uri.parse("package:" + packageName))
                    startActivity(intent)
                },
                onNegativeClick = {
                    // User chose not to disable optimization, hide the card for now
                    binding.batteryOptimizationCard.visibility = View.GONE
                }
            )
        }
    }

    private fun checkBatteryOptimizationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                binding.batteryOptimizationCard.visibility = View.GONE
            } else {
                binding.batteryOptimizationCard.visibility = View.VISIBLE
            }
        } else {
            binding.batteryOptimizationCard.visibility = View.GONE
        }
    }

    private fun setupPresetsRecyclerView() {
        val presets = listOf(
            BlueLightPreset(getString(R.string.night_mode_preset_name), R.color.night_mode),
            BlueLightPreset(getString(R.string.candle_preset_name), R.color.candle),
            BlueLightPreset(getString(R.string.sunset_preset_name), R.color.sunset),
            BlueLightPreset(getString(R.string.lamp_preset_name), R.color.lamp),
            BlueLightPreset(getString(R.string.room_light_preset_name), R.color.room_light),
            BlueLightPreset(getString(R.string.sun_preset_name), R.color.sun),
            BlueLightPreset(getString(R.string.twilight_preset_name), R.color.twilight),
            BlueLightPreset(getString(R.string.warm_white_preset_name), R.color.warm_white),
            BlueLightPreset(getString(R.string.cool_white_preset_name), R.color.cool_white),
            BlueLightPreset(getString(R.string.daylight_preset_name), R.color.daylight),
            BlueLightPreset(getString(R.string.deep_red_preset_name), R.color.deep_red),
            BlueLightPreset(getString(R.string.soft_blue_preset_name), R.color.soft_blue)
        )
        val adapter = PresetAdapter(presets) { preset ->
            viewModel.setSelectedPreset(preset.name)
        }
        binding.presetsRecyclerview.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.filterSettings.observe(this) { settings ->
            binding.mainToggleButton.text = if (settings.isEnabled) getString(R.string.disable_filter) else getString(R.string.enable_filter)
            binding.statusIndicator.text = if (settings.isEnabled) getString(R.string.filter_enabled) else getString(R.string.filter_disabled)
            binding.intensitySeekbar.progress = settings.intensity
            binding.intensityPercentageText.text = "${settings.intensity}%"
            binding.screenDimSeekbar.progress = settings.dimLevel
            binding.screenDimPercentageText.text = "${settings.dimLevel}%"
            binding.presetsRecyclerview.isEnabled = settings.isEnabled
            binding.scheduleSwitch.isChecked = settings.scheduleEnabled
            binding.startTimeValue.text = settings.scheduleStartTime
            binding.endTimeValue.text = settings.scheduleEndTime

            }
    }private fun setupClickListeners() {
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