package com.nighteyecare.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nighteyecare.app.data.database.AppDatabase
import com.nighteyecare.app.data.repository.FilterSettingsRepository
import com.nighteyecare.app.services.FilterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (context != null) {
                val database = AppDatabase.getDatabase(context)
                val repository = FilterSettingsRepository(database.filterSettingsDao())
                CoroutineScope(Dispatchers.IO).launch {
                    val filterSettings = repository.getFilterSettings()
                    if (filterSettings?.isEnabled == true) {
                        val serviceIntent = Intent(context, FilterService::class.java)
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}
