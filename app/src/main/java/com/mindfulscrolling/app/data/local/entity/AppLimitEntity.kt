package com.mindfulscrolling.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey val packageName: String,
    val limitDurationMinutes: Int, // Daily limit in minutes
    val isGroupLimit: Boolean = false,
    val groupName: String? = null,
    val notificationIntervalMinutes: Int? = null // Per-app usage popup interval (e.g., 15, 30, 60 min)
)
