package com.homio.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.homio.app.models.User

class RegisterActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var rgUserType: RadioGroup
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        rgUserType = findViewById(R.id.rgUserType)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        btnRegister.setOnClickListener { attemptRegister() }
        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptRegister() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.register_error_empty_fields), Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.register_error_password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.register_error_password_short), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedId = rgUserType.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, getString(R.string.register_error_select_type), Toast.LENGTH_SHORT).show()
            return
        }
        val userType = if (selectedId == R.id.rbLandlord) "landlord" else "tenant"

        // Firebase Auth hashes and stores the password securely - we never
        // handle or store the raw password ourselves.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = User(
                    userId = uid,
                    fullName = fullName,
                    email = email,
                    userType = userType
                )
                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        val intent = if (userType == "landlord") {
                            Intent(this, LandlordDashboardActivity::class.java)
                        } else {
                            Intent(this, TenantDashboardActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.register_failed, e.message), Toast.LENGTH_LONG).show()
            }
    }
}
