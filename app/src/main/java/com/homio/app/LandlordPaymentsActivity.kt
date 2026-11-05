package com.homio.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.homio.app.data.api.ApiClient
import com.homio.app.data.api.ApiErrors
import com.homio.app.data.api.PaymentDto
import com.homio.app.data.api.VerifyBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LandlordPaymentsActivity : BaseActivity() {

    private lateinit var llList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landlord_payments)

        llList = findViewById(R.id.llPaymentsList)
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        loadPayments()
    }

    private fun loadPayments() {
        lifecycleScope.launch {
            val response = try {
                ApiClient.service.getPayments()
            } catch (e: Exception) {
                null
            }

            when {
                response == null -> showMessage(getString(R.string.error_no_connection))
                !response.isSuccessful -> showMessage(getString(R.string.payment_load_failed))
                else -> {
                    // Waiting-for-review first; the server's newest-first order is kept inside each group
                    val items = response.body()?.items.orEmpty()
                        .sortedBy { if (it.status == Labels.PAYMENT_PENDING) 0 else 1 }
                    render(items)
                }
            }
        }
    }

    private fun render(items: List<PaymentDto>) {
        if (items.isEmpty()) {
            showMessage(getString(R.string.no_payments))
            return
        }

        llList.removeAllViews()
        for (payment in items) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_payment, llList, false)
            val tenantName = payment.tenantName?.takeIf { it.isNotBlank() }
                ?: getString(R.string.role_tenant)
            PaymentUi.bind(this, row, payment, tenantName)

            val id = payment.id
            if (id != null) {
                row.findViewById<View>(R.id.llPaymentActions).visibility = View.VISIBLE
                row.findViewById<View>(R.id.btnViewProof).setOnClickListener { viewProof(id) }

                // Only a payment that is still waiting can be approved or rejected
                val pending = payment.status == Labels.PAYMENT_PENDING
                val approve = row.findViewById<View>(R.id.btnApprove)
                val reject = row.findViewById<View>(R.id.btnReject)
                approve.visibility = if (pending) View.VISIBLE else View.GONE
                reject.visibility = if (pending) View.VISIBLE else View.GONE
                approve.setOnClickListener { verify(id, "approve") }
                reject.setOnClickListener { askRejectReason(id) }
            }
            llList.addView(row)
        }
    }

    private fun viewProof(id: String) {
        lifecycleScope.launch {
            val bitmap = try {
                val response = ApiClient.service.getProof(id)
                val bytes = if (response.isSuccessful) {
                    withContext(Dispatchers.IO) { response.body()?.bytes() }
                } else null
                bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            } catch (e: Exception) {
                null
            }

            if (bitmap == null) {
                toast(getString(R.string.proof_load_failed))
                return@launch
            }

            val image = ImageView(this@LandlordPaymentsActivity).apply {
                setImageBitmap(bitmap)
                adjustViewBounds = true
            }
            // A scroll view so tall screenshots can be scrolled
            val scroll = ScrollView(this@LandlordPaymentsActivity).apply {
                setPadding(dp(16), dp(16), dp(16), dp(8))
                addView(image)
            }
            AlertDialog.Builder(this@LandlordPaymentsActivity)
                .setView(scroll)
                .setPositiveButton(R.string.action_close, null)
                .show()
        }
    }

    private fun askRejectReason(id: String) {
        val input = EditText(this).apply { hint = getString(R.string.hint_reject_reason) }
        val container = FrameLayout(this).apply {
            setPadding(dp(24), dp(8), dp(24), 0)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_reject_title)
            .setView(container)
            .setPositiveButton(R.string.btn_reject) { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    toast(getString(R.string.error_reject_reason))
                    return@setPositiveButton
                }
                verify(id, "reject", reason)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun verify(id: String, decision: String, reason: String? = null) {
        lifecycleScope.launch {
            val response = try {
                ApiClient.service.verifyPayment(id, VerifyBody(decision, reason))
            } catch (e: Exception) {
                null
            }

            when {
                response == null -> toast(getString(R.string.error_no_connection))
                response.isSuccessful -> {
                    toast(
                        getString(
                            if (decision == "approve") R.string.payment_approved
                            else R.string.payment_rejected
                        )
                    )
                    loadPayments()
                }
                else -> toast(
                    getString(
                        R.string.action_failed,
                        ApiErrors.message(response) ?: response.code().toString()
                    )
                )
            }
        }
    }

    private fun showMessage(message: String) {
        llList.removeAllViews()
        val text = TextView(this)
        text.text = message
        text.setTextColor(ContextCompat.getColor(this, R.color.homio_gray))
        llList.addView(text)
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}