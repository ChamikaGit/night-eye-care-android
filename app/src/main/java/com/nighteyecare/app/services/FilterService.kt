package com.nighteyecare.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import com.nighteyecare.app.R
import com.nighteyecare.app.ui.activities.MainActivity
import com.nighteyecare.app.utils.AppPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilterService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: LinearLayout
    private var isFilterActive: Boolean = false
    private var currentFilterColor: Int = 0
    private var currentFilterAlpha: Int = 0

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "NightEyeCareChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LinearLayout(this)
        overlayView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Permission not granted, stop the service
            stopSelf()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val appPreferencesManager = AppPreferencesManager(this@FilterService)
            if (appPreferencesManager.getNotificationsEnabled()) {
                createNotificationChannel()
                startForeground(NOTIFICATION_ID, createNotification(isFilterActive))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Simplified logic to bypass the 'val' reassignment error
        val colorValue: Int = intent?.getIntExtra("color", 0) ?: 0
        val alphaValue: Int = intent?.getIntExtra("alpha", 0) ?: 0
        currentFilterColor = colorValue
        currentFilterAlpha = alphaValue
        isFilterActive = true // Assume filter is active when started with color/alpha
        applyFilter()
        updateNotification()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    private fun applyFilter() {
        if (isFilterActive) {
            overlayView.setBackgroundColor(currentFilterColor)
            overlayView.background.alpha = currentFilterAlpha
        } else {
            overlayView.background.alpha = 0 // Make it transparent when paused
        }
    }

    private fun updateNotification() {
        CoroutineScope(Dispatchers.Main).launch {
            val appPreferencesManager = AppPreferencesManager(this@FilterService)
            if (appPreferencesManager.getNotificationsEnabled()) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, createNotification(isFilterActive))
            } else {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Night Eye Care Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(isActive: Boolean) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(if (isActive) getString(R.string.notification_active_text) else getString(R.string.notification_paused_text))
        .setSmallIcon(R.drawable.ic_notification_icon) // Replace with your app icon
        .setOngoing(true)
        .addAction(0, if (isActive) getString(R.string.notification_action_pause) else getString(R.string.notification_action_resume), getPendingIntent("ACTION_TOGGLE_FILTER"))
        .addAction(0, getString(R.string.notification_action_stop), getPendingIntent("ACTION_STOP_SERVICE"))
        .addAction(0, getString(R.string.notification_action_open_app), getPendingIntent("ACTION_OPEN_APP"))
        .build()

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = when (action) {
            "ACTION_TOGGLE_FILTER" -> Intent(this, FilterService::class.java).apply { this.action = action }
            "ACTION_STOP_SERVICE" -> Intent(this, FilterService::class.java).apply { this.action = action }
            "ACTION_OPEN_APP" -> Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            else -> throw IllegalArgumentException("Invalid action")
        }
        return PendingIntent.getService(this, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}