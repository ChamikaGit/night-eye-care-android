package com.nighteyecare.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nighteyecare.app.services.FilterService
import com.nighteyecare.app.ui.activities.MainActivity

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d("NotificationActionReceiver", "Received action: $action")

        context?.let {
            when (action) {
                "ACTION_TOGGLE_FILTER" -> {
                    val serviceIntent = Intent(it, FilterService::class.java).apply {
                        this.action = action
                    }
                    it.startService(serviceIntent)
                }
                "ACTION_STOP_SERVICE" -> {
                    val serviceIntent = Intent(it, FilterService::class.java).apply {
                        this.action = action
                    }
                    it.startService(serviceIntent)
                }
                "ACTION_OPEN_APP" -> {
                    val launchIntent = Intent(it, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    it.startActivity(launchIntent)
                }
                else -> Log.w("NotificationActionReceiver", "Unknown action: $action")
            }
        }
    }
}