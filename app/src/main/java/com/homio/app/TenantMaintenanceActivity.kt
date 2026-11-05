package com.homio.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.homio.app.data.MaintenanceEntity
import com.homio.app.data.MaintenanceRepository
import com.homio.app.data.NetworkUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TenantMaintenanceActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repo: MaintenanceRepository
    private lateinit var llList: LinearLayout
    private var syncListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_maintenance)

        auth = FirebaseAuth.getInstance()
        repo = MaintenanceRepository.get(this)
        llList = findViewById(R.id.llMaintenanceList)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_maintenance)

        findViewById<android.widget.Button>(R.id.btnReportProblem).setOnClickListener {
            showReportDialog()
        }

        // The list always comes from Room, so it works offline and updates instantly
        val uid = auth.currentUser?.uid
        if (uid != null) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    repo.observeForTenant(uid).collect { render(it) }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // While online, Firestore keeps the Room cache up to date
        syncListener?.remove()
        syncListener = auth.currentUser?.uid?.let { repo.startSync(it) }
    }

    override fun onStop() {
        super.onStop()
        syncListener?.remove()
        syncListener = null
    }

    private fun render(items: List<MaintenanceEntity>) {
        llList.removeAllViews()
        if (items.isEmpty()) {
            val empty = TextView(this)
            empty.text = getString(R.string.no_maintenance_requests)
            empty.setTextColor(ContextCompat.getColor(this, R.color.homio_gray))
            llList.addView(empty)
            return
        }

        for (item in items) {
            val dateStr = if (item.createdDate > 0) {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(item.createdDate))
            } else ""

            var subtitle = getString(
                R.string.item_subtitle, Labels.category(this, item.category), dateStr
            )
            if (item.pendingSync) {
                subtitle = getString(R.string.item_subtitle, subtitle, getString(R.string.waiting_to_sync))
            }

            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_maintenance_request, llList, false)

            itemView.findViewById<TextView>(R.id.tvItemTitle).text =
                item.title.ifEmpty { getString(R.string.untitled) }
            itemView.findViewById<TextView>(R.id.tvItemSubtitle).text = subtitle

            val statusView = itemView.findViewById<TextView>(R.id.tvItemStatus)
            statusView.text = Labels.status(this, item.status)
            val color = when (item.status) {
                Labels.STATUS_COMPLETED -> R.color.homio_green
                Labels.STATUS_IN_PROGRESS -> R.color.homio_orange
                else -> R.color.homio_purple
            }
            statusView.backgroundTintList = ContextCompat.getColorStateList(this, color)

            llList.addView(itemView)
        }
    }

    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_problem, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etReportTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etReportDescription)
        val spCategory = dialogView.findViewById<Spinner>(R.id.spCategory)
        spCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Labels.categoryLabels(this)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_report_title)
            .setView(dialogView)
            .setPositiveButton(R.string.action_submit) { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                // Save the English key, not the translated label, so data stays consistent for every language
                val category = Labels.CATEGORY_KEYS.getOrElse(spCategory.selectedItemPosition) {
                    Labels.CATEGORY_OTHER
                }

                if (title.isEmpty()) {
                    Toast.makeText(this, getString(R.string.error_enter_title), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                lifecycleScope.launch {
                    // Saved to Room straight away; WorkManager uploads it when online
                    repo.createRequest(uid, title, description, category)
                    val message = if (NetworkUtil.isOnline(this@TenantMaintenanceActivity)) {
                        R.string.request_submitted
                    } else {
                        R.string.request_saved_offline
                    }
                    Toast.makeText(
                        this@TenantMaintenanceActivity, getString(message), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}