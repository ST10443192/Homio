package com.homio.app

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class RealtimeNotifier(private val app: HomioApp) {

    private val db = FirebaseFirestore.getInstance()
    private val registrations = mutableListOf<ListenerRegistration>()
    private var activeKey: String? = null

    /** Safe to call repeatedly - only restarts if the user or role changed. */
    fun start(uid: String, userType: String) {
        val key = "$uid:$userType"
        if (key == activeKey) return
        stop()
        activeKey = key

        if (userType == "landlord") {
            listenForNewRequests()
            listenForNewPayments()
            listenForMessages("tenant", LandlordMessagesActivity::class.java, R.string.notif_message_from_tenant)
        } else {
            listenForStatusChanges(uid)
            listenForPaymentUpdates(uid)
            listenForMessages("landlord", TenantMessagesActivity::class.java, R.string.notif_message_from_landlord)
        }
    }

    fun stop() {
        registrations.forEach { it.remove() }
        registrations.clear()
        activeKey = null
    }

    private fun localized(): Context = LocaleHelper.applyLocale(app)

    // ---------- Maintenance ----------

    private fun listenForNewRequests() {
        var baselineDone = false
        registrations += db.collection("maintenance").addSnapshotListener { snap, error ->
            if (error != null || snap == null) {
                Log.w(TAG, "Request listener failed", error)
                return@addSnapshotListener
            }
            val canNotify = baselineDone
            if (!baselineDone && !snap.metadata.isFromCache) baselineDone = true
            if (!canNotify || app.isOnScreen(LandlordMaintenanceActivity::class.java)) {
                return@addSnapshotListener
            }

            val ctx = localized()
            for (change in snap.documentChanges) {
                if (change.type != DocumentChange.Type.ADDED) continue
                val doc = change.document
                val title = doc.getString("title") ?: ctx.getString(R.string.untitled)
                val category = Labels.category(ctx, doc.getString("category") ?: "")
                NotificationHelper.show(
                    context = app,
                    channelId = NotificationHelper.CHANNEL_REQUESTS,
                    notificationId = doc.id.hashCode(),
                    title = ctx.getString(R.string.notif_new_request_title),
                    text = ctx.getString(R.string.item_subtitle, title, category),
                    target = LandlordMaintenanceActivity::class.java
                )
            }
        }
    }

    private fun listenForStatusChanges(uid: String) {
        var baselineDone = false
        val lastStatus = HashMap<String, String>()
        registrations += db.collection("maintenance")
            .whereEqualTo("tenantId", uid)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    Log.w(TAG, "Status listener failed", error)
                    return@addSnapshotListener
                }
                val canNotify = baselineDone
                if (!baselineDone && !snap.metadata.isFromCache) baselineDone = true
                val onScreen = app.isOnScreen(TenantMaintenanceActivity::class.java)
                val ctx = localized()

                for (doc in snap.documents) {
                    val status = doc.getString("status") ?: Labels.STATUS_SUBMITTED
                    val previous = lastStatus[doc.id]
                    lastStatus[doc.id] = status

                    if (canNotify && !onScreen && previous != null && previous != status) {
                        NotificationHelper.show(
                            context = app,
                            channelId = NotificationHelper.CHANNEL_REQUESTS,
                            notificationId = doc.id.hashCode(),
                            title = ctx.getString(R.string.notif_status_title),
                            text = ctx.getString(
                                R.string.notif_status_text,
                                doc.getString("title") ?: ctx.getString(R.string.untitled),
                                Labels.status(ctx, status)
                            ),
                            target = TenantMaintenanceActivity::class.java
                        )
                    }
                }
            }
    }

    // ---------- Payments ----------

    /** Landlord: someone just submitted proof of payment. */
    private fun listenForNewPayments() {
        var baselineDone = false
        registrations += db.collection("payments").addSnapshotListener { snap, error ->
            if (error != null || snap == null) {
                Log.w(TAG, "Payment listener failed", error)
                return@addSnapshotListener
            }
            val canNotify = baselineDone
            if (!baselineDone && !snap.metadata.isFromCache) baselineDone = true
            if (!canNotify || app.isOnScreen(LandlordPaymentsActivity::class.java)) {
                return@addSnapshotListener
            }

            val ctx = localized()
            for (change in snap.documentChanges) {
                if (change.type != DocumentChange.Type.ADDED) continue
                val doc = change.document
                if (doc.getString("status") != Labels.PAYMENT_PENDING) continue

                val tenant = doc.getString("tenantName")?.takeIf { it.isNotBlank() }
                    ?: ctx.getString(R.string.role_tenant)
                NotificationHelper.show(
                    context = app,
                    channelId = NotificationHelper.CHANNEL_PAYMENTS,
                    notificationId = doc.id.hashCode(),
                    title = ctx.getString(R.string.notif_new_payment_title),
                    text = ctx.getString(
                        R.string.item_subtitle, tenant, PaymentUi.money(doc.getDouble("amount"))
                    ),
                    target = LandlordPaymentsActivity::class.java
                )
            }
        }
    }

    /** Tenant: the landlord approved or rejected one of my payments. */
    private fun listenForPaymentUpdates(uid: String) {
        var baselineDone = false
        val lastStatus = HashMap<String, String>()
        registrations += db.collection("payments")
            .whereEqualTo("tenantId", uid)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    Log.w(TAG, "Payment update listener failed", error)
                    return@addSnapshotListener
                }
                val canNotify = baselineDone
                if (!baselineDone && !snap.metadata.isFromCache) baselineDone = true
                val onScreen = app.isOnScreen(TenantRentActivity::class.java)
                val ctx = localized()

                for (doc in snap.documents) {
                    val status = doc.getString("status") ?: Labels.PAYMENT_PENDING
                    val previous = lastStatus[doc.id]
                    lastStatus[doc.id] = status

                    if (!canNotify || onScreen || previous == null || previous == status) continue

                    val month = doc.getString("month") ?: ""
                    val text = when (status) {
                        Labels.PAYMENT_VERIFIED ->
                            ctx.getString(R.string.notif_payment_verified_text, month)
                        Labels.PAYMENT_REJECTED -> {
                            val reason = doc.getString("rejectionReason")
                            if (reason.isNullOrBlank()) {
                                ctx.getString(R.string.notif_payment_rejected_plain, month)
                            } else {
                                ctx.getString(R.string.notif_payment_rejected_text, month, reason)
                            }
                        }
                        else -> continue
                    }

                    NotificationHelper.show(
                        context = app,
                        channelId = NotificationHelper.CHANNEL_PAYMENTS,
                        notificationId = doc.id.hashCode(),
                        title = ctx.getString(R.string.notif_payment_update_title),
                        text = text,
                        target = TenantRentActivity::class.java
                    )
                }
            }
    }

    // ---------- Messages ----------

    private fun listenForMessages(fromRole: String, screen: Class<*>, titleRes: Int) {
        var baselineDone = false
        // Only messages from the OTHER role, so you're never notified about your own
        registrations += db.collection("messages")
            .whereEqualTo("senderRole", fromRole)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    Log.w(TAG, "Message listener failed", error)
                    return@addSnapshotListener
                }
                val canNotify = baselineDone
                if (!baselineDone && !snap.metadata.isFromCache) baselineDone = true
                if (!canNotify || app.isOnScreen(screen)) return@addSnapshotListener

                val ctx = localized()
                val newest = snap.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .mapNotNull { it.document.getString("text") }
                    .lastOrNull() ?: return@addSnapshotListener

                NotificationHelper.show(
                    context = app,
                    channelId = NotificationHelper.CHANNEL_MESSAGES,
                    notificationId = NotificationHelper.ID_MESSAGES,
                    title = ctx.getString(titleRes),
                    text = newest,
                    target = screen
                )
            }
    }

    private companion object { const val TAG = "RealtimeNotifier" }
}