package com.nighteyecare.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.nighteyecare.app.services.FilterService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val pm = context?.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NightEyeCare::AlarmReceiverWakeLock")
        wakeLock?.acquire(5000) // Acquire for 5 seconds, should be enough to start/stop service

        try {
            val action = intent?.action
            if (context != null) {
                val serviceIntent = Intent(context, FilterService::class.java)
                if (action == "com.nighteyecare.app.ACTION_START_FILTER") {
                    // Start the filter service
                    context.startService(serviceIntent)
                } else if (action == "com.nighteyecare.app.ACTION_STOP_FILTER") {
                    // Stop the filter service
                    context.stopService(serviceIntent)
                }
            }
        } finally {
            wakeLock?.release()
        }
    }
}
