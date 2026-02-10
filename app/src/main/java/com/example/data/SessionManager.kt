package com.example.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val KEY_THEME = "app_theme"

    fun saveTheme(isDark: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean("dark_theme", isDark)
        editor.apply()
    }
    fun isDarkTheme(): Boolean {
        return prefs.getBoolean("dark_theme", false)
    }
    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }
    fun saveUserId(id: String) {
        prefs.edit().putString("userId", id).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("userId", null)
    }
    fun setProfileCompleted(done: Boolean) {
        prefs.edit().putBoolean("profileCompleted", done).apply()
    }

    fun isProfileCompleted(): Boolean {
        return prefs.getBoolean("profileCompleted", false)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
