package com.mindfulscrolling.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey val packageName: String,
    val limitDurationMinutes: Int, // Daily limit in minutes
    val isGroupLimit: Boolean = false,
    val groupName: String? = null
)
