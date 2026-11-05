package com.homio.app

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("homio_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPref.getString("selected_language", "en") ?: "en"
        super.attachBaseContext(updateLocale(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the system back button globally across all extending activities
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isTaskRoot) {
                    finishAffinity() // Safely exit if at the root screen
                } else {
                    finish() // Close current activity and reveal previous screen
                }
            }
        })
    }

    // Handles the Action Bar / Toolbar navigation up button
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Global helper for requesting notification permissions on Android 13+ (Tiramisu)
    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    companion object {
        /**
         * Call this method from your Language Selection Settings/Screen when the user picks a new language.
         * Example: BaseActivity.setAppLanguage(context, "es")
         */
        fun setAppLanguage(context: Context, languageCode: String) {
            // Save selection to SharedPreferences
            val sharedPref = context.getSharedPreferences("homio_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().putString("selected_language", languageCode).apply()

            // Update Android System per-app language settings
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        private fun updateLocale(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
}