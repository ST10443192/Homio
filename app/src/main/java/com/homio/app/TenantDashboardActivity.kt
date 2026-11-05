package com.homio.app

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TenantDashboardActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_dashboard)

        FirebaseAuth.getInstance().currentUser?.uid?.let {
            (application as HomioApp).startNotifications(it, "tenant")
        }
        requestNotificationPermissionIfNeeded()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_home)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvOpenRequestsCount = findViewById<TextView>(R.id.tvOpenRequestsCount)

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

            // Live count of this tenant's maintenance requests that are not yet completed.
            // Rent/lease card values above are placeholders until Property <-> Tenant
            // linking is added - noted for the final PoE.
            db.collection("maintenance")
                .whereEqualTo("tenantId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val openCount = snapshot.documents.count { it.getString("status") != Labels.STATUS_COMPLETED }
                    tvOpenRequestsCount.text = openCount.toString()
                }
        }

        findViewById<TextView>(R.id.tvViewRequests).setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_maintenance
        }
    }
}