package com.mindfulscrolling.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "break_preferences")

@Singleton
class BreakPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val BREAK_ACTIVE = booleanPreferencesKey("break_active")
        val BREAK_END_TIME = longPreferencesKey("break_end_time")
        val BREAK_WHITELIST = stringSetPreferencesKey("break_whitelist")
    }

    val isBreakActive: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BREAK_ACTIVE] ?: false
    }

    val breakEndTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[BREAK_END_TIME] ?: 0L
    }

    val breakWhitelist: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[BREAK_WHITELIST] ?: setOf(
            "com.android.settings",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.android.phone",
            "com.android.incallui",
            "com.mindfulscrolling.app" // Self
        )
    }

    suspend fun setBreakActive(active: Boolean, endTime: Long = 0L) {
        dataStore.edit { preferences ->
            preferences[BREAK_ACTIVE] = active
            if (active) {
                preferences[BREAK_END_TIME] = endTime
            }
        }
    }

    suspend fun updateWhitelist(whitelist: Set<String>) {
        dataStore.edit { preferences ->
            preferences[BREAK_WHITELIST] = whitelist
        }
    }
}
