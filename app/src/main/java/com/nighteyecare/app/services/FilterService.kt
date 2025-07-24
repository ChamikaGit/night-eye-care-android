package com.nighteyecare.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.nighteyecare.app.R
import com.nighteyecare.app.ui.activities.MainActivity
import com.nighteyecare.app.utils.AppPreferencesManager
import com.nighteyecare.app.receivers.NotificationActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.nighteyecare.app.data.database.AppDatabase
import com.nighteyecare.app.data.model.BlueLightPreset

class FilterService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: LinearLayout
    private lateinit var dimOverlayView: LinearLayout
    private lateinit var appPreferencesManager: AppPreferencesManager
    private var currentFilterColor: Int = 0
    private var currentFilterAlpha: Int = 0

    private val presets = listOf(
        BlueLightPreset("Night Mode", R.color.night_mode),
        BlueLightPreset("Candle", R.color.candle),
        BlueLightPreset("Sunset", R.color.sunset),
        BlueLightPreset("Lamp", R.color.lamp),
        BlueLightPreset("Room Light", R.color.room_light),
        BlueLightPreset("Sun", R.color.sun),
        BlueLightPreset("Twilight", R.color.twilight),
        BlueLightPreset("Warm White", R.color.warm_white),
        BlueLightPreset("Cool White", R.color.cool_white),
        BlueLightPreset("Daylight", R.color.daylight),
        BlueLightPreset("Deep Red", R.color.deep_red),
        BlueLightPreset("Soft Blue", R.color.soft_blue)
    )
    

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

        dimOverlayView = LinearLayout(this)
        dimOverlayView.layoutParams = LinearLayout.LayoutParams(
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

        val dimParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
        windowManager.addView(dimOverlayView, dimParams)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Permission not granted, stop the service
            stopSelf()
            return
        }

        createNotificationChannel()
        this.appPreferencesManager = AppPreferencesManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d("FilterService", "Received action: $action")

        when (action) {
            "ACTION_TOGGLE_FILTER" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val currentSettings = AppDatabase.getDatabase(this@FilterService.applicationContext).filterSettingsDao().getFilterSettings() ?: return@launch
                    val newIsActive = !appPreferencesManager.getFilterActive()
                    appPreferencesManager.setFilterActive(newIsActive)
                    val updatedSettings = currentSettings.copy(isEnabled = newIsActive)
                    AppDatabase.getDatabase(applicationContext).filterSettingsDao().insertOrUpdate(updatedSettings)
                    applyFilter(newIsActive, updatedSettings.dimLevel)
                    updateNotification(newIsActive)
                }
            }
            "ACTION_STOP_SERVICE" -> {
                stopSelf()
            }
            "ACTION_UPDATE_NOTIFICATION_STATE" -> {
                intent?.let {
                    val isActive = it.getBooleanExtra("isActive", false)
                    updateNotification(isActive)
                }
            }
            else -> {
                // Handle initial start or start with color/alpha/dimLevel
                CoroutineScope(Dispatchers.IO).launch {
                    val savedSettings = AppDatabase.getDatabase(applicationContext).filterSettingsDao().getFilterSettings()
                    val savedIsActive = savedSettings?.isEnabled ?: false

                    currentFilterColor = intent?.getIntExtra("color", presets.find { preset: BlueLightPreset -> preset.name == savedSettings?.selectedPreset }?.colorRes ?: 0) ?: 0
                    currentFilterAlpha = intent?.getIntExtra("alpha", savedSettings?.intensity ?: 0) ?: 0
                    val dimLevel = intent?.getIntExtra("dimLevel", savedSettings?.dimLevel ?: 0) ?: 0
                    val isActive = intent?.getBooleanExtra("isActive", savedIsActive) ?: savedIsActive

                    applyFilter(isActive, dimLevel)
                    updateNotification(isActive)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
        windowManager.removeView(dimOverlayView)
        // Service is stopped, so remove notification
        stopForeground(true)
    }

    private fun applyFilter(isActive: Boolean, dimLevel: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (isActive) {
                overlayView.setBackgroundColor(currentFilterColor)
                overlayView.background.alpha = currentFilterAlpha
                dimOverlayView.setBackgroundColor(ContextCompat.getColor(this@FilterService, R.color.black_dim))
                dimOverlayView.background.alpha = (dimLevel * 2.55).toInt() // 0-100 to 0-255
            } else {
                if (overlayView.background == null) overlayView.setBackgroundDrawable(ColorDrawable(0))
                overlayView.background.alpha = 0 // Make it transparent when paused
                if (dimOverlayView.background == null) dimOverlayView.setBackgroundDrawable(ColorDrawable(0))
                dimOverlayView.background.alpha = 0 // Make it transparent when paused
            }
        }
    }

    private fun updateNotification(isActive: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(isActive))
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

        // Set content for the normal view
        notificationLayout.setTextViewText(R.id.notification_title, getString(R.string.notification_title))
        notificationLayout.setTextViewText(R.id.notification_text, if (isActive) getString(R.string.notification_active_text) else getString(R.string.notification_paused_text))
        notificationLayout.setImageViewResource(R.id.notification_action_toggle, if (isActive) R.drawable.ic_pause else R.drawable.ic_play_arrow)
        notificationLayout.setInt(R.id.notification_action_toggle, "setColorFilter", ContextCompat.getColor(this, R.color.accent_color))
        notificationLayout.setInt(R.id.notification_action_stop, "setColorFilter", ContextCompat.getColor(this, R.color.accent_color))
        notificationLayout.setInt(R.id.notification_action_open_app, "setColorFilter", ContextCompat.getColor(this, R.color.accent_color))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_toggle, getPendingIntent("ACTION_TOGGLE_FILTER"))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_stop, getPendingIntent("ACTION_STOP_SERVICE"))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_open_app, getPendingIntent("ACTION_OPEN_APP"))

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setCustomContentView(notificationLayout)
            .setOngoing(true)
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