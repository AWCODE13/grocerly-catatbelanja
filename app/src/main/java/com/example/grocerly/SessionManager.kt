package com.example.grocerly.utils

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("login_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGIN = "is_login"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"

        private fun photoKey(email: String) = "photo_uri_$email"
    }

    fun setLogin(isLogin: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGIN, isLogin).apply()
    }

    fun login(email: String, password: String): Boolean {
        val savedEmail = getEmail()
        val savedPassword = getPassword()

        if (savedEmail.isEmpty() && savedPassword.isEmpty()) {
            setEmail(email)
            setPassword(password)
            setLogin(true)
            return true
        }

        if (email == savedEmail && password == savedPassword) {
            setLogin(true)
            return true
        }

        return false
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGIN, false)
            .apply()
    }

    fun isLogin(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGIN, false)
    }

    fun setEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    fun getPassword(): String {
        return prefs.getString(KEY_PASSWORD, "") ?: ""
    }

    fun setPhotoUri(email: String, uri: String) {
        if (email.isNotBlank()) {
            prefs.edit().putString(photoKey(email), uri).apply()
        }
    }

    fun getPhotoUri(email: String): String {
        return prefs.getString(photoKey(email), "") ?: ""
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "id") ?: "id"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "light") ?: "light"
    }
}
