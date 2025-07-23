package com.nighteyecare.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.nighteyecare.app.services.FilterService

class FilterManager(private val context: Context) {

    fun startFilterService(color: Int, alpha: Int, isActive: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            context.startActivity(intent)
        } else {
            val intent = Intent(context, FilterService::class.java).apply {
                putExtra("color", color)
                putExtra("alpha", alpha)
                putExtra("isActive", isActive)
            }
            context.startService(intent)
        }
    }

    fun stopFilterService() {
        val intent = Intent(context, FilterService::class.java)
        context.stopService(intent)
    }
}