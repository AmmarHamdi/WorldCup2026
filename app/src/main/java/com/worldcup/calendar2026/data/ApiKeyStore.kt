package com.worldcup.calendar2026.data

import android.content.Context
import androidx.core.content.edit
import com.worldcup.calendar2026.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "worldcup_prefs"
private const val KEY_API_KEY = "api_key"

@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val defaultApiKey: String = BuildConfig.FOOTBALL_API_KEY

    fun getKey(): String = prefs.getString(KEY_API_KEY, defaultApiKey) ?: defaultApiKey

    fun setKey(key: String) {
        prefs.edit { putString(KEY_API_KEY, key.trim()) }
    }
}
