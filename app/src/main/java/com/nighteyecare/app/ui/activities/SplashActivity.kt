package com.nighteyecare.app.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nighteyecare.app.ui.viewmodels.SplashViewModel
import com.nighteyecare.app.ui.viewmodels.SplashViewModelFactory
import com.nighteyecare.app.utils.AppPreferencesManager

class SplashActivity : AppCompatActivity() {

    private lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = SplashViewModelFactory(application, AppPreferencesManager(this))
        splashViewModel = ViewModelProvider(this, factory).get(SplashViewModel::class.java)

        splashViewModel.navigateTo.observe(this) {
            startActivity(Intent(this, it))
            finish()
        }

        splashViewModel.checkOnboardingStatus()
    }
}
