package com.worldcup.calendar2026.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "worldcup_prefs"
private const val KEY_API_KEY = "api_key"
private const val DEFAULT_API_KEY = "a123e0f2ef9102c5419c40f0f91cc270"

@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getKey(): String = prefs.getString(KEY_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY

    fun setKey(key: String) {
        prefs.edit { putString(KEY_API_KEY, key.trim()) }
    }
}
