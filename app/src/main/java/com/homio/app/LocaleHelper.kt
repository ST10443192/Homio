package com.homio.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "homio_prefs"
    private const val KEY_LANGUAGE = "app_language"

    const val LANG_ENGLISH = "en"
    const val LANG_AFRIKAANS = "af"
    const val LANG_ZULU = "zu"

    /** Languages the app has translations for (res/values, values-af, values-zu). */
    val SUPPORTED_LANGUAGES = setOf(LANG_ENGLISH, LANG_AFRIKAANS, LANG_ZULU)

    /**
     * The saved language if there is one. On first launch we use the phone's
     * language when we support it, otherwise English.
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved != null && saved in SUPPORTED_LANGUAGES) return saved

        val deviceLanguage = Locale.getDefault().language
        return if (deviceLanguage in SUPPORTED_LANGUAGES) deviceLanguage else LANG_ENGLISH
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun applyLocale(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        // Copy the configuration rather than mutating the shared one
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
