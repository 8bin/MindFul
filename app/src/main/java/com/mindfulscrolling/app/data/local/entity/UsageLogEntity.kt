package com.mindfulscrolling.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val date: Long, // Timestamp for the start of the day (midnight)
    val durationMillis: Long,
    val lastUpdated: Long
)
