package com.nighteyecare.app.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nighteyecare.app.ui.activities.MainActivity
import com.nighteyecare.app.ui.activities.TutorialActivity
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(2000) // 2 second splash screen

            val appPreferencesManager = AppPreferencesManager(this@SplashActivity)
            val onboardingCompleted = appPreferencesManager.isOnboardingCompleted()

            if (onboardingCompleted) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, TutorialActivity::class.java))
            }
            finish()
        }
    }
}
