package com.parentdashboard.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    private const val PREFS_NAME = "locale_sync"
    private const val KEY_LANGUAGE = "language"

    fun applyAppLocale(languageCode: String) {
        val target = localeListFor(languageCode)
        val current = AppCompatDelegate.getApplicationLocales()
        if (localeListsEqual(current, target)) return
        AppCompatDelegate.setApplicationLocales(target)
    }

    fun persistLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun getPersistedLanguage(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "system") ?: "system"

    fun syncFromPreferences(context: Context) {
        applyAppLocale(getPersistedLanguage(context))
    }

    private fun localeListFor(languageCode: String): LocaleListCompat =
        if (languageCode == "system") {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode.replace('_', '-'))
        }

    private fun localeListsEqual(a: LocaleListCompat, b: LocaleListCompat): Boolean {
        if (a.isEmpty && b.isEmpty) return true
        return a.toLanguageTags() == b.toLanguageTags()
    }
}
