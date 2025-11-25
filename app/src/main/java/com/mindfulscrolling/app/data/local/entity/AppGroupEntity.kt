package com.mindfulscrolling.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_groups")
data class AppGroupEntity(
    @PrimaryKey val groupName: String,
    val limitDurationMinutes: Int
)
