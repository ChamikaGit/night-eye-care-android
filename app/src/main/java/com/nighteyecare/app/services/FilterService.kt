package com.nighteyecare.app.services

import android.app.Notification
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
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews
import com.nighteyecare.app.R
import com.nighteyecare.app.ui.activities.MainActivity
import com.nighteyecare.app.utils.AppPreferencesManager
import com.nighteyecare.app.receivers.NotificationActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FilterService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: LinearLayout
    private var currentFilterColor: Int = 0
    private var currentFilterAlpha: Int = 0
    private lateinit var appPreferencesManager: AppPreferencesManager

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "NightEyeCareChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        appPreferencesManager = AppPreferencesManager(this)
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
            if (appPreferencesManager.getNotificationsEnabled()) {
                createNotificationChannel()
                startForeground(NOTIFICATION_ID, createNotification(appPreferencesManager.getFilterActive()))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "ACTION_TOGGLE_FILTER" -> {
                val isActive = runBlocking { appPreferencesManager.getFilterActive() }
                runBlocking { appPreferencesManager.setFilterActive(!isActive) }
                runBlocking { applyFilter() }
                runBlocking { updateNotification() }
            }
            "ACTION_STOP_SERVICE" -> {
                runBlocking { appPreferencesManager.setFilterActive(false) }
                stopSelf()
            }
            "ACTION_OPEN_APP" -> {
                val launchIntent = Intent(this@FilterService, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                startActivity(launchIntent)
            }
            else -> {
                // Initial start or start with color/alpha
                val colorValue: Int = intent?.getIntExtra("color", 0) ?: 0
                val alphaValue: Int = intent?.getIntExtra("alpha", 0) ?: 0
                currentFilterColor = colorValue
                currentFilterAlpha = alphaValue
                runBlocking { appPreferencesManager.setFilterActive(true) } // Assume filter is active when started with color/alpha
                runBlocking { applyFilter() }
                runBlocking { updateNotification() }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    private fun applyFilter() {
        CoroutineScope(Dispatchers.Main).launch {
            val isActive = appPreferencesManager.getFilterActive()
            if (isActive) {
                overlayView.setBackgroundColor(currentFilterColor)
                overlayView.background.alpha = currentFilterAlpha
            } else {
                overlayView.background.alpha = 0 // Make it transparent when paused
            }
        }
    }

    private fun updateNotification() {
        CoroutineScope(Dispatchers.Main).launch {
            if (appPreferencesManager.getNotificationsEnabled()) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, createNotification(appPreferencesManager.getFilterActive()))
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

    private fun createNotification(isActive: Boolean): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.custom_notification)

        notificationLayout.setTextViewText(R.id.notification_title, getString(R.string.notification_title))
        notificationLayout.setTextViewText(R.id.notification_text, if (isActive) getString(R.string.notification_active_text) else getString(R.string.notification_paused_text))

        // Set icon for toggle button based on filter state
        val toggleIcon = if (isActive) R.drawable.ic_pause else R.drawable.ic_play_arrow
        notificationLayout.setImageViewResource(R.id.notification_action_toggle, toggleIcon)

        // Set pending intents for buttons
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_toggle, getPendingIntent("ACTION_TOGGLE_FILTER"))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_stop, getPendingIntent("ACTION_STOP_SERVICE"))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_open_app, getPendingIntent("ACTION_OPEN_APP"))

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout) // For expanded view
            .setOngoing(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        Log.d("FilterService", "Creating PendingIntent for action: $action")
        val intent = when (action) {
            "ACTION_TOGGLE_FILTER" -> Intent(this, NotificationActionReceiver::class.java).apply { this.action = action }
            "ACTION_STOP_SERVICE" -> Intent(this, NotificationActionReceiver::class.java).apply { this.action = action }
            "ACTION_OPEN_APP" -> Intent(this, NotificationActionReceiver::class.java).apply { this.action = action }
            else -> throw IllegalArgumentException("Invalid action")
        }
        return PendingIntent.getBroadcast(this, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}