package com.sentinel.protection.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "supabase_settings")

object SupabaseConfig {

    // Default configuration (can be updated by user in settings or build configuration)
    const val DEFAULT_SUPABASE_URL = "https://your-project.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY = "your-anon-key-here"

    private val KEY_URL = stringPreferencesKey("supabase_url")
    private val KEY_ANON = stringPreferencesKey("supabase_anon_key")

    fun getSupabaseUrl(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_URL] ?: DEFAULT_SUPABASE_URL
        }
    }

    fun getSupabaseAnonKey(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_ANON] ?: DEFAULT_SUPABASE_ANON_KEY
        }
    }

    suspend fun saveCredentials(context: Context, url: String, anonKey: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_URL] = url.trim()
            preferences[KEY_ANON] = anonKey.trim()
        }
    }
}
