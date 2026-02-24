package com.mindfulscrolling.app.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)
