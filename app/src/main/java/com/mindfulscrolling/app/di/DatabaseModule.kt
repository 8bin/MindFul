package com.mindfulscrolling.app.di

import android.content.Context
import androidx.room.Room
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
        ).build()
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
