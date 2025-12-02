package com.mindfulscrolling.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mindfulscrolling.app.data.local.AppDatabase
import com.mindfulscrolling.app.data.local.dao.AppGroupDao
import com.mindfulscrolling.app.data.local.dao.AppLimitDao
import com.mindfulscrolling.app.data.local.dao.OverrideLogDao
import com.mindfulscrolling.app.data.local.dao.UsageLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mindful_db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed default profile
                java.util.concurrent.Executors.newSingleThreadExecutor().execute {
                    val profileId = 1L
                    val contentValues = android.content.ContentValues().apply {
                        put("id", profileId)
                        put("name", "Essential")
                        put("icon", "🌟")
                        put("isActive", false)
                        put("scheduleEnabled", false)
                    }
                    db.insert("focus_profiles", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, contentValues)

                    // Seed essential apps
                    val essentialApps = listOf(
                        "com.android.phone",
                        "com.google.android.dialer",
                        "com.android.contacts",
                        "com.google.android.contacts",
                        "com.android.settings",
                        "com.google.android.apps.messaging",
                        "com.android.mms",
                        "com.whatsapp", // Common essential
                        "com.google.android.apps.nbu.paisa.user" // GPay
                    )

                    essentialApps.forEach { packageName ->
                        val appValues = android.content.ContentValues().apply {
                            put("profileId", profileId)
                            put("packageName", packageName)
                            put("limitDurationMinutes", -1L) // Whitelisted
                        }
                        db.insert("profile_app_cross_ref", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, appValues)
                    }
                }
            }
        })
        .build()
    }

    @Provides
    fun provideAppLimitDao(database: AppDatabase): AppLimitDao {
        return database.appLimitDao()
    }

    @Provides
    fun provideUsageLogDao(database: AppDatabase): UsageLogDao {
        return database.usageLogDao()
    }

    @Provides
    fun provideAppGroupDao(database: AppDatabase): AppGroupDao {
        return database.appGroupDao()
    }

    @Provides
    fun provideOverrideLogDao(database: AppDatabase): OverrideLogDao {
        return database.overrideLogDao()
    }

    @Provides
    fun provideFocusProfileDao(database: AppDatabase): com.mindfulscrolling.app.data.local.dao.FocusProfileDao {
        return database.focusProfileDao()
    }
}
