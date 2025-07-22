package com.nighteyecare.app.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nighteyecare.app.databinding.ActivityPresetInfoBinding

class PresetInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresetInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
