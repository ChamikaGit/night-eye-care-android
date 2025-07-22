package com.nighteyecare.app.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.nighteyecare.app.databinding.ActivityTutorialBinding
import com.nighteyecare.app.ui.adapters.TutorialPagerAdapter
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.launch

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagerAdapter = TutorialPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        binding.skipButton.setOnClickListener {
            lifecycleScope.launch {
                AppPreferencesManager(this@TutorialActivity).setOnboardingCompleted(true)
                startActivity(Intent(this@TutorialActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem < pagerAdapter.itemCount - 1) {
                binding.viewPager.currentItem++
            } else {
                lifecycleScope.launch {
                    AppPreferencesManager(this@TutorialActivity).setOnboardingCompleted(true)
                    startActivity(Intent(this@TutorialActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
