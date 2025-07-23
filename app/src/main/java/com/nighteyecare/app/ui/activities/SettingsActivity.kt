package com.nighteyecare.app.ui.activities

import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nighteyecare.app.databinding.ActivitySettingsBinding
import com.nighteyecare.app.ui.viewmodels.SettingsViewModel
import com.nighteyecare.app.ui.viewmodels.SettingsViewModelFactory
import com.nighteyecare.app.utils.AppPreferencesManager

import androidx.activity.OnBackPressedCallback
import com.nighteyecare.app.ui.utils.CustomToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var customToolbar: CustomToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = SettingsViewModelFactory(application, AppPreferencesManager(this))
        settingsViewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        customToolbar = CustomToolbar(binding.toolbar.root)
        customToolbar.setTitle(getString(com.nighteyecare.app.R.string.settings_button_description))
        customToolbar.setBackButtonClickListener { onBackPressedDispatcher.onBackPressed() }

        setupClickListeners()
        settingsViewModel.appPreferences.observe(this) { prefs ->
            prefs?.let {
                binding.notificationsSwitch.isChecked = it.notificationsEnabled
            }
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                binding.appVersionSetting.text = "App Version: ${pInfo.versionName}"
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                binding.appVersionSetting.text = "App Version: N/A"
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupClickListeners() {
        binding.languageSetting.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        }

        binding.batteryOptimizationSetting.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setNotificationsEnabled(isChecked)
        }

        binding.feedbackSetting.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@nighteyecare.com"))
                putExtra(Intent.EXTRA_SUBJECT, "NightEyeCare App Feedback")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        binding.privacyPolicySetting.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nighteyecare.com/privacy_policy")) // Placeholder URL
            startActivity(intent)
        }
    }

    
}
