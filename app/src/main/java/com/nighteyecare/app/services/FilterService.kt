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

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(false))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val colorValue: Int = intent?.getIntExtra("color", 0) ?: 0
        val alphaValue: Int = intent?.getIntExtra("alpha", 0) ?: 0
        val isActive: Boolean = intent?.getBooleanExtra("isActive", false) ?: false

        currentFilterColor = colorValue
        currentFilterAlpha = alphaValue

        when (action) {
            "ACTION_TOGGLE_FILTER" -> {
                // This action is now handled by the ViewModel, which will restart the service with the correct state
            }
            "ACTION_STOP_SERVICE" -> {
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
            }
        }
        applyFilter(isActive)
        updateNotification(isActive)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    private fun applyFilter(isActive: Boolean) {
        if (isActive) {
            overlayView.setBackgroundColor(currentFilterColor)
            overlayView.background.alpha = currentFilterAlpha
        } else {
            overlayView.background.alpha = 0 // Make it transparent when paused
        }
    }

    private fun updateNotification(isActive: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isActive) {
            notificationManager.notify(NOTIFICATION_ID, createNotification(isActive))
        } else {
            notificationManager.cancel(NOTIFICATION_ID)
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
        val notificationExpandedLayout = RemoteViews(packageName, R.layout.custom_notification_expanded)

        // Set content for the normal view
        notificationLayout.setTextViewText(R.id.notification_title, getString(R.string.notification_title))
        notificationLayout.setTextViewText(R.id.notification_text, if (isActive) getString(R.string.notification_active_text) else getString(R.string.notification_paused_text))
        notificationLayout.setImageViewResource(R.id.notification_action_toggle, if (isActive) R.drawable.ic_pause else R.drawable.ic_play_arrow)
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_toggle, getPendingIntent("ACTION_TOGGLE_FILTER"))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_stop, getPendingIntent("ACTION_STOP_SERVICE"))
        notificationLayout.setOnClickPendingIntent(R.id.notification_action_open_app, getPendingIntent("ACTION_OPEN_APP"))

        // Set content for the expanded view
        notificationExpandedLayout.setTextViewText(R.id.notification_title_expanded, getString(R.string.notification_title))
        notificationExpandedLayout.setTextViewText(R.id.notification_text_expanded, if (isActive) getString(R.string.notification_active_text) else getString(R.string.notification_paused_text))
        notificationExpandedLayout.setImageViewResource(R.id.notification_action_toggle_expanded, if (isActive) R.drawable.ic_pause else R.drawable.ic_play_arrow)
        notificationExpandedLayout.setOnClickPendingIntent(R.id.notification_action_toggle_expanded, getPendingIntent("ACTION_TOGGLE_FILTER"))
        notificationExpandedLayout.setOnClickPendingIntent(R.id.notification_action_stop_expanded, getPendingIntent("ACTION_STOP_SERVICE"))
        notificationExpandedLayout.setOnClickPendingIntent(R.id.notification_action_open_app_expanded, getPendingIntent("ACTION_OPEN_APP"))

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationExpandedLayout) // Use the expanded layout here
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