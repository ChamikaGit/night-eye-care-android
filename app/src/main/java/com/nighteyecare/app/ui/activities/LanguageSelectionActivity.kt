package com.nighteyecare.app.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nighteyecare.app.databinding.ActivityLanguageSelectionBinding
import com.nighteyecare.app.ui.adapters.LanguageAdapter
import androidx.activity.OnBackPressedCallback
import com.nighteyecare.app.ui.utils.CustomToolbar
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.launch

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private lateinit var customToolbar: CustomToolbar
    private lateinit var appPreferencesManager: AppPreferencesManager
    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreferencesManager = AppPreferencesManager(this)

        customToolbar = CustomToolbar(binding.toolbar.root)
        customToolbar.setTitle(getString(com.nighteyecare.app.R.string.language))
        customToolbar.setBackButtonClickListener { onBackPressedDispatcher.onBackPressed() }

        setupLanguageRecyclerView()
        loadSelectedLanguage()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupLanguageRecyclerView() {
        val languages = listOf(
            Pair("English", "en"),
            Pair("Tiếng Việt (Vietnamese)", "vi"),
            Pair("Filipino (Filipino)", "fil"),
            Pair("bahasa Indonesia (Indonesian)", "in"),
            Pair("แบบไทย (Thai)", "th"),
            Pair("ខ្មែរ (Khmer)", "km")
        )

        languageAdapter = LanguageAdapter(languages) {
            selectedLanguageCode ->
            lifecycleScope.launch {
                val currentPrefs = appPreferencesManager.getAppPreferences() ?: return@launch
                appPreferencesManager.saveAppPreferences(currentPrefs.copy(selectedLanguage = selectedLanguageCode))
                languageAdapter.setSelectedLanguage(selectedLanguageCode)
                // TODO: Implement actual language change and activity recreation
            }
        }

        binding.languageRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = languageAdapter
        }
    }

    private fun loadSelectedLanguage() {
        lifecycleScope.launch {
            val currentPrefs = appPreferencesManager.getAppPreferences()
            currentPrefs?.selectedLanguage?.let {
                languageAdapter.setSelectedLanguage(it)
            }
        }
    }
}
