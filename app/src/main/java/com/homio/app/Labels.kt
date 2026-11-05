package com.homio.app

import android.content.Context

/**
 * Firestore stores maintenance statuses and categories as fixed English keys
 * ("In Progress", "Plumbing", ...) so data stays consistent between users who
 * use different languages. This object turns those keys into text in the
 * current app language. Never save the translated text back to Firestore.
 */
object Labels {

    const val STATUS_SUBMITTED = "Submitted"
    const val STATUS_IN_PROGRESS = "In Progress"
    const val STATUS_COMPLETED = "Completed"

    const val CATEGORY_OTHER = "Other"

    /** Order shown in the "Report a Problem" spinner. */
    val CATEGORY_KEYS = arrayOf("Plumbing", "Electrical", "Appliance", "Structural", CATEGORY_OTHER)

    fun status(context: Context, key: String): String = when (key) {
        STATUS_SUBMITTED -> context.getString(R.string.status_submitted)
        STATUS_IN_PROGRESS -> context.getString(R.string.status_in_progress)
        STATUS_COMPLETED -> context.getString(R.string.status_completed)
        else -> key
    }

    fun category(context: Context, key: String): String = when (key) {
        "Plumbing" -> context.getString(R.string.category_plumbing)
        "Electrical" -> context.getString(R.string.category_electrical)
        "Appliance" -> context.getString(R.string.category_appliance)
        "Structural" -> context.getString(R.string.category_structural)
        CATEGORY_OTHER -> context.getString(R.string.category_other)
        else -> key
    }

    fun categoryLabels(context: Context): Array<String> =
        CATEGORY_KEYS.map { category(context, it) }.toTypedArray()

    const val PAYMENT_PENDING = "Pending"
    const val PAYMENT_VERIFIED = "Verified"
    const val PAYMENT_REJECTED = "Rejected"

    fun paymentStatus(context: Context, key: String): String = when (key) {
        PAYMENT_PENDING -> context.getString(R.string.payment_status_pending)
        PAYMENT_VERIFIED -> context.getString(R.string.payment_status_verified)
        PAYMENT_REJECTED -> context.getString(R.string.payment_status_rejected)
        else -> key
    }
}
