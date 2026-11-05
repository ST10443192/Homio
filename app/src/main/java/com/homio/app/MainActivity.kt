package com.homio.app

import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                val userType = doc.getString("userType") ?: "tenant"
                routeToDashboard(userType)
            }
            .addOnFailureListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }

    private fun routeToDashboard(userType: String) {
        val intent = if (userType == "landlord") {
            Intent(this, LandlordDashboardActivity::class.java)
        } else {
            Intent(this, TenantDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}