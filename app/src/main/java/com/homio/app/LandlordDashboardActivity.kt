package com.homio.app

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.view.View

class LandlordDashboardActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_dashboard)

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            (application as HomioApp).startNotifications(it, "landlord")
        }
        requestNotificationPermissionIfNeeded()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupLandlordNav(this, bottomNav, R.id.nav_home_l)

        findViewById<View>(R.id.cardPayments).setOnClickListener {
            startActivity(Intent(this, LandlordPaymentsActivity::class.java))
        }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvPropertiesCount = findViewById<TextView>(R.id.tvPropertiesCount)
        val tvOpenRequestsCount = findViewById<TextView>(R.id.tvOpenRequestsCount)
        // Tenants count is a placeholder ("0") until Property <-> Tenant linking
        // is built - a good next feature for the final PoE.

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("fullName")
                    tvWelcome.text = if (name.isNullOrEmpty()) {
                        getString(R.string.greeting_default)
                    } else {
                        getString(R.string.greeting_with_name, name)
                    }
                }

            db.collection("properties")
                .whereEqualTo("landlordId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    tvPropertiesCount.text = snapshot.size().toString()
                }

            // Simplified for the prototype: shows all open requests across tenants
            // rather than only ones tied to this landlord's own properties.
            db.collection("maintenance")
                .get()
                .addOnSuccessListener { snapshot ->
                    val openCount = snapshot.documents.count { it.getString("status") != Labels.STATUS_COMPLETED }
                    tvOpenRequestsCount.text = openCount.toString()
                }
        }
    }
}
