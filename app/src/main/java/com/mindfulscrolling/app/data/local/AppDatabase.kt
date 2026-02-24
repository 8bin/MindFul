package com.mindfulscrolling.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mindfulscrolling.app.data.local.dao.AppGroupDao
import com.mindfulscrolling.app.data.local.dao.AppLimitDao
import com.mindfulscrolling.app.data.local.dao.OverrideLogDao
import com.mindfulscrolling.app.data.local.dao.UsageLogDao
import com.mindfulscrolling.app.data.local.entity.AppGroupEntity
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.data.local.entity.OverrideLogEntity
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity
import com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef
import com.mindfulscrolling.app.data.local.dao.FocusProfileDao

@Database(
    entities = [
        AppLimitEntity::class,
        UsageLogEntity::class,
        AppGroupEntity::class,
        OverrideLogEntity::class,
        FocusProfileEntity::class,
        ProfileAppCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appLimitDao(): AppLimitDao
    abstract fun usageLogDao(): UsageLogDao
    abstract fun appGroupDao(): AppGroupDao
    abstract fun overrideLogDao(): OverrideLogDao
    abstract fun focusProfileDao(): FocusProfileDao
}
