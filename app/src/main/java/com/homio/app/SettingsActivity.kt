package com.homio.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.homio.app.data.MaintenanceRepository

class SettingsActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button
    private lateinit var radioGroupLanguage: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage)

        val currentLang = getSharedPreferences("homio_prefs", MODE_PRIVATE).getString("selected_language", "en") ?: "en"
        when (currentLang) {
            "zu" -> radioGroupLanguage.check(R.id.radioZulu)
            "af" -> radioGroupLanguage.check(R.id.radioAfrikaans)
            else -> radioGroupLanguage.check(R.id.radioEnglish)
        }

        loadProfile()

        btnSave.setOnClickListener { saveProfile() }

        radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.radioZulu -> "zu"
                R.id.radioAfrikaans -> "af"
                else -> "en"
            }
            if (languageCode != currentLang) {
                BaseActivity.setAppLanguage(this, languageCode)

                val uid = auth.currentUser?.uid
                if (uid != null) {
                    db.collection("users").document(uid).update("language", languageCode)
                }

                reopenWithoutAnimation()
            }
        }

        btnLogout.setOnClickListener {
            MaintenanceRepository.get(this).clearLocalData()
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun reopenWithoutAnimation() {
        val restart = Intent(this, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(restart)
        finish()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etFullName.setText(doc.getString("fullName") ?: "")
                etPhone.setText(doc.getString("phone") ?: "")
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "fullName" to etFullName.text.toString().trim(),
            "phone" to etPhone.text.toString().trim()
        )
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${getString(R.string.settings_save_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}