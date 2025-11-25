package com.mindfulscrolling.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mindfulscrolling.app.R
import com.mindfulscrolling.app.domain.repository.SettingsRepository
import com.mindfulscrolling.app.domain.repository.UsageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppMonitoringService : Service() {

    @Inject
    lateinit var usageRepository: UsageRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startMonitoring()
    }

    private fun startForegroundService() {
        val channelId = "monitoring_service_channel"
        val channelName = "App Monitoring Service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mindful")
            .setContentText("Monitoring your digital wellness")
            .setSmallIcon(R.drawable.ic_app_icon)
            .build()

        startForeground(1, notification)
    }

    @Inject
    lateinit var overlayManager: com.mindfulscrolling.app.service.OverlayManager

    @Inject
    lateinit var permissionManager: com.mindfulscrolling.app.domain.manager.PermissionManager

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                if (!permissionManager.hasUsageStatsPermission()) {
                    delay(5000)
                    continue
                }

                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(
                    android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 10,
                    time
                )

                if (stats != null && stats.isNotEmpty()) {
                    val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
                    val currentApp = sortedStats.firstOrNull()?.packageName

                    if (currentApp != null && currentApp != packageName) { // Don't monitor self
                        checkLimit(currentApp)
                    }
                }
                delay(1000)
            }
        }
    }

    private suspend fun checkLimit(packageName: String) {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Update usage
        usageRepository.logUsage(packageName, 1000, today)

        // Check limit
        val limit = settingsRepository.getLimit(packageName)
        if (limit != null) {
            val usage = usageRepository.getUsageForApp(packageName, today)
            val usedMillis = usage?.durationMillis ?: 0
            val limitMillis = limit.limitDurationMinutes * 60 * 1000L

            if (usedMillis >= limitMillis) {
                with(Dispatchers.Main) {
                    if (permissionManager.hasOverlayPermission()) {
                         kotlinx.coroutines.withContext(Dispatchers.Main) {
                            overlayManager.showOverlay(
                                packageName = packageName,
                                usageDuration = usedMillis,
                                limitDuration = limitMillis,
                                onContinue = {
                                    // Extend limit by 5 mins (temp) - Logic to be added
                                },
                                onTakeBreak = {
                                    val startMain = Intent(Intent.ACTION_MAIN)
                                    startMain.addCategory(Intent.CATEGORY_HOME)
                                    startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(startMain)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
