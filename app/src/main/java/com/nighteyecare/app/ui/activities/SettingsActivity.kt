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
import androidx.lifecycle.lifecycleScope
import com.nighteyecare.app.databinding.ActivitySettingsBinding
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var appPreferencesManager: AppPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreferencesManager = AppPreferencesManager(this)

        setupLanguageSpinner()
        setupClickListeners()
        loadSettings()
    }

    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(com.nighteyecare.app.R.array.languages)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]
                lifecycleScope.launch {
                    val currentPrefs = appPreferencesManager.getAppPreferences() ?: return@launch
                    appPreferencesManager.saveAppPreferences(currentPrefs.copy(selectedLanguage = selectedLanguage))
                    // TODO: Implement actual language change and activity recreation if needed
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupClickListeners() {
        binding.batteryOptimizationSetting.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                appPreferencesManager.setNotificationsEnabled(isChecked)
            }
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

    private fun loadSettings() {
        lifecycleScope.launch {
            val currentPrefs = appPreferencesManager.getAppPreferences()
            currentPrefs?.let {
                val languages = resources.getStringArray(com.nighteyecare.app.R.array.languages)
                val selectedLanguageIndex = languages.indexOf(it.selectedLanguage)
                if (selectedLanguageIndex != -1) {
                    binding.languageSpinner.setSelection(selectedLanguageIndex)
                }
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
    }
}
