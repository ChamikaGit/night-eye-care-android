package com.nighteyecare.app.ui.activities

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nighteyecare.app.databinding.ActivityPresetInfoBinding
import com.nighteyecare.app.ui.utils.CustomToolbar

class PresetInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresetInfoBinding
    private lateinit var customToolbar: CustomToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customToolbar = CustomToolbar(binding.toolbar.root)
        customToolbar.setTitle(getString(com.nighteyecare.app.R.string.preset_information))
        customToolbar.setBackButtonClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
