package com.homio.app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ListenerRegistration
import com.homio.app.data.MaintenanceEntity
import com.homio.app.data.MaintenanceRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// NOTE: For simplicity in the prototype this shows ALL maintenance requests
// rather than filtering to only this landlord's own properties, since
// Property <-> Tenant <-> Landlord linking isn't built yet. That linking is
// a good next step for the final PoE.
class LandlordMaintenanceActivity : BaseActivity() {

    private lateinit var repo: MaintenanceRepository
    private lateinit var llList: LinearLayout
    private var syncListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_maintenance)

        repo = MaintenanceRepository.get(this)
        llList = findViewById(R.id.llRequestsList)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupLandlordNav(this, bottomNav, R.id.nav_maintenance_l)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeAll().collect { render(it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        syncListener?.remove()
        syncListener = repo.startSync(null)   // null = all requests
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
                .inflate(R.layout.item_maintenance_request_landlord, llList, false)

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

            val btnComplete = itemView.findViewById<android.widget.Button>(R.id.btnMarkComplete)
            if (item.status == Labels.STATUS_COMPLETED) {
                btnComplete.isEnabled = false
                btnComplete.text = getString(R.string.status_completed)
            } else {
                btnComplete.setOnClickListener {
                    // Updates Room instantly (the screen refreshes), then uploads when online
                    lifecycleScope.launch { repo.markComplete(item.id) }
                }
            }

            llList.addView(itemView)
        }
    }
}