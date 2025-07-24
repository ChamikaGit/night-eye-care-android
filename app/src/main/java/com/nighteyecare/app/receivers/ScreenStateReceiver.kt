package com.nighteyecare.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nighteyecare.app.services.FilterService
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val appPreferencesManager = AppPreferencesManager(it)
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    Log.d("ScreenStateReceiver", "Screen ON or User PRESENT")
                    CoroutineScope(Dispatchers.IO).launch {
                        val isFilterActive = appPreferencesManager.getFilterActive()
                        if (isFilterActive) {
                            Log.d("ScreenStateReceiver", "Filter was active, restarting service to re-apply overlay.")
                            val serviceIntent = Intent(it, FilterService::class.java)
                            // No need to pass color/alpha/dimLevel here, service will restore from preferences
                            it.startService(serviceIntent)
                        }
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d("ScreenStateReceiver", "Screen OFF")
                    // Optionally, you could stop the overlay here to save battery
                    // but the requirement is to keep the service running.
                }
                else -> {
                    Log.d("ScreenStateReceiver", "Unhandled action: ${intent?.action}")
                }
            }
        }
    }
}