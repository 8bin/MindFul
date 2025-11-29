package com.mindfulscrolling.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_profiles")
data class FocusProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Resource name or emoji
    val isActive: Boolean = false,
    val scheduleEnabled: Boolean = false,
    val startTime: Int? = null, // Minutes from midnight
    val endTime: Int? = null, // Minutes from midnight
    val daysOfWeek: String? = null // Comma separated days (1=Sunday, 7=Saturday)
)
