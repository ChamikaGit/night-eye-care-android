package com.nighteyecare.app.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.nighteyecare.app.databinding.ActivityTutorialBinding
import com.nighteyecare.app.ui.adapters.TutorialPagerAdapter
import com.nighteyecare.app.utils.AppPreferencesManager
import com.nighteyecare.app.ui.viewmodels.TutorialViewModel
import com.nighteyecare.app.ui.viewmodels.TutorialViewModelFactory

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private lateinit var tutorialViewModel: TutorialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = TutorialViewModelFactory(application, AppPreferencesManager(this))
        tutorialViewModel = ViewModelProvider(this, factory).get(TutorialViewModel::class.java)

        val pagerAdapter = TutorialPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        binding.skipButton.setOnClickListener {
            tutorialViewModel.setOnboardingCompleted()
            startActivity(Intent(this@TutorialActivity, MainActivity::class.java))
            finish()
        }

        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem < pagerAdapter.itemCount - 1) {
                binding.viewPager.currentItem++
            } else {
                tutorialViewModel.setOnboardingCompleted()
                startActivity(Intent(this@TutorialActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
