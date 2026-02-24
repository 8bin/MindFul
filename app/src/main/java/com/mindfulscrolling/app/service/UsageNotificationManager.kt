package com.mindfulscrolling.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mindfulscrolling.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "usage_alerts"
        const val CHANNEL_NAME = "Usage Alerts"
        private const val PREF_NAME = "usage_notification_prefs"
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_INTERVAL = "notification_interval_minutes"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // Track last notification time per app to debounce
    private val lastNotifiedMap = mutableMapOf<String, Long>()
    
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic alerts about your app usage time"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var intervalMinutes: Int
        get() = prefs.getInt(KEY_INTERVAL, 30)
        set(value) = prefs.edit().putInt(KEY_INTERVAL, value).apply()

    /**
     * Check if a notification should be sent for the given app.
     * Called from the monitoring service after each usage update.
     *
     * @param packageName the app being monitored
     * @param totalUsageMillis total usage for today so far
     * @param appName human-readable app name
     * @param perAppIntervalMinutes per-app interval override (from AppLimitEntity), null uses global
     */
    fun checkAndNotify(packageName: String, totalUsageMillis: Long, appName: String, perAppIntervalMinutes: Int? = null) {
        if (!isEnabled) return
        
        // Per-app interval overrides global setting
        val effectiveInterval = perAppIntervalMinutes ?: intervalMinutes
        val intervalMs = effectiveInterval * 60 * 1000L
        if (intervalMs <= 0) return
        
        // How many intervals have been crossed?
        val intervalsUsed = (totalUsageMillis / intervalMs).toInt()
        if (intervalsUsed <= 0) return
        
        // What's the milestone we should have notified for?
        val milestoneMs = intervalsUsed * intervalMs
        
        // Has this milestone already been notified?
        val lastNotified = lastNotifiedMap[packageName] ?: 0L
        if (lastNotified >= milestoneMs) return
        
        // Fire notification
        lastNotifiedMap[packageName] = milestoneMs
        
        val usedMinutes = (totalUsageMillis / 60000).toInt()
        val displayTime = if (usedMinutes >= 60) {
            "${usedMinutes / 60}h ${usedMinutes % 60}m"
        } else {
            "${usedMinutes}m"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle("⏱️ Usage Alert")
            .setContentText("You've spent $displayTime on $appName today")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        // Use packageName hashcode as notification ID so each app has its own notification
        notificationManager.notify(packageName.hashCode(), notification)
    }

    fun resetForNewDay() {
        lastNotifiedMap.clear()
    }
}
