package com.homio.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.homio.app.data.api.ApiClient
import com.homio.app.data.api.PaymentDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// NOTE: Monthly rent / due date / deposit are still placeholders until Property <-> Tenant
// linking is built. The current-month card and payment history are real and come from the API.
class TenantRentActivity : BaseActivity() {

    private lateinit var llHistory: LinearLayout
    private lateinit var tvCurrentAmount: TextView
    private lateinit var tvCurrentStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_rent)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_rent)

        llHistory = findViewById(R.id.llPaymentHistory)
        tvCurrentAmount = findViewById(R.id.tvCurrentRentAmount)
        tvCurrentStatus = findViewById(R.id.tvCurrentRentStatus)

        findViewById<Button>(R.id.btnSubmitProof).setOnClickListener {
            startActivity(Intent(this, SubmitPaymentActivity::class.java))
        }
    }

    // Reload every time we come back, so a payment just submitted shows up straight away
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
                response == null -> showHistoryMessage(getString(R.string.error_no_connection))
                !response.isSuccessful -> showHistoryMessage(getString(R.string.payment_load_failed))
                else -> render(response.body()?.items.orEmpty())
            }
        }
    }

    private fun render(items: List<PaymentDto>) {
        showCurrentMonth(items)
        if (items.isEmpty()) {
            showHistoryMessage(getString(R.string.no_payment_history))
            return
        }

        llHistory.removeAllViews()
        for (payment in items) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_payment, llHistory, false)
            row.background = null   // the history box is already a card
            PaymentUi.bind(this, row, payment, getString(R.string.nav_rent))
            llHistory.addView(row)
        }
    }

    private fun showCurrentMonth(items: List<PaymentDto>) {
        val thisMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val forMonth = items.filter { it.month == thisMonth }
        // A rejected payment doesn't count, unless it's the only one for this month
        val current = forMonth.firstOrNull { it.status != Labels.PAYMENT_REJECTED }
            ?: forMonth.firstOrNull()

        if (current == null) {
            tvCurrentAmount.text = getString(R.string.placeholder_amount_zero)
            tvCurrentStatus.text = getString(R.string.no_payment_recorded)
        } else {
            tvCurrentAmount.text = PaymentUi.money(current.amount)
            tvCurrentStatus.text =
                Labels.paymentStatus(this, current.status ?: Labels.PAYMENT_PENDING)
        }
    }

    private fun showHistoryMessage(message: String) {
        llHistory.removeAllViews()
        val text = TextView(this)
        text.text = message
        text.textSize = 13f
        text.setTextColor(ContextCompat.getColor(this, R.color.homio_gray))
        llHistory.addView(text)
    }
}