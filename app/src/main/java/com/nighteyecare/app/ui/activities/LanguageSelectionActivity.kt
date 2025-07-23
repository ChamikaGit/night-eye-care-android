package com.nighteyecare.app.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nighteyecare.app.databinding.ActivityLanguageSelectionBinding
import com.nighteyecare.app.ui.adapters.LanguageAdapter
import androidx.activity.OnBackPressedCallback
import com.nighteyecare.app.ui.utils.CustomToolbar
import com.nighteyecare.app.utils.AppPreferencesManager
import com.nighteyecare.app.ui.viewmodels.LanguageSelectionViewModel
import com.nighteyecare.app.ui.viewmodels.LanguageSelectionViewModelFactory

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private lateinit var customToolbar: CustomToolbar
    private lateinit var languageSelectionViewModel: LanguageSelectionViewModel
    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = LanguageSelectionViewModelFactory(application, AppPreferencesManager(this))
        languageSelectionViewModel = ViewModelProvider(this, factory).get(LanguageSelectionViewModel::class.java)

        customToolbar = CustomToolbar(binding.toolbar.root)
        customToolbar.setTitle(getString(com.nighteyecare.app.R.string.language))
        customToolbar.setBackButtonClickListener { onBackPressedDispatcher.onBackPressed() }

        setupLanguageRecyclerView()
        languageSelectionViewModel.selectedLanguageCode.observe(this) {
            languageAdapter.setSelectedLanguage(it)
        }

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
            languageSelectionViewModel.setSelectedLanguage(selectedLanguageCode)
        }

        binding.languageRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = languageAdapter
        }
    }

    
}
