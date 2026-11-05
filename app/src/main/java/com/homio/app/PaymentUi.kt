package com.homio.app

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.homio.app.data.api.PaymentDto
import java.text.NumberFormat
import java.util.Locale

object PaymentUi {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    fun money(amount: Double?): String = currencyFormat.format(amount ?: 0.0)

    /** Fills one row made from item_payment.xml. */
    fun bind(context: Context, row: View, payment: PaymentDto, title: String) {
        row.findViewById<TextView>(R.id.tvPaymentTitle).text = title
        row.findViewById<TextView>(R.id.tvPaymentSubtitle).text =
            context.getString(R.string.item_subtitle, payment.month ?: "", money(payment.amount))

        val reference = row.findViewById<TextView>(R.id.tvPaymentReference)
        if (payment.reference.isNullOrEmpty()) {
            reference.visibility = View.GONE
        } else {
            reference.visibility = View.VISIBLE
            reference.text = context.getString(R.string.payment_reference_line, payment.reference)
        }

        val status = payment.status ?: Labels.PAYMENT_PENDING
        val statusView = row.findViewById<TextView>(R.id.tvPaymentStatus)
        statusView.text = Labels.paymentStatus(context, status)
        val color = when (status) {
            Labels.PAYMENT_VERIFIED -> R.color.homio_green
            Labels.PAYMENT_REJECTED -> R.color.homio_red
            else -> R.color.homio_orange
        }
        statusView.backgroundTintList = ContextCompat.getColorStateList(context, color)

        val reason = row.findViewById<TextView>(R.id.tvRejectReason)
        if (status == Labels.PAYMENT_REJECTED && !payment.rejectionReason.isNullOrEmpty()) {
            reason.visibility = View.VISIBLE
            reason.text = context.getString(R.string.payment_rejected_reason, payment.rejectionReason)
        } else {
            reason.visibility = View.GONE
        }
    }
}