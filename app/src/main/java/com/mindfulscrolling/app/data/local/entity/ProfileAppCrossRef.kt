package com.mindfulscrolling.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "profile_app_cross_ref",
    primaryKeys = ["profileId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = FocusProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId"), Index("packageName")]
)
data class ProfileAppCrossRef(
    val profileId: Long,
    val packageName: String,
    val limitDurationMinutes: Long // 0 means blocked, -1 means unlimited (whitelist)
)
