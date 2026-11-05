package com.homio.app.data.api

import com.google.gson.Gson
import retrofit2.Response

// ----- Maintenance -----

data class CreateMaintenanceBody(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String? = null   // left out: the server uses "Medium"
)

data class StatusBody(val status: String)

data class MaintenanceDto(
    val id: String?,
    val ticketNumber: String?,
    val status: String?
)

// ----- Payments -----

data class PaymentDto(
    val id: String?,
    val tenantName: String?,
    val amount: Double?,
    val month: String?,
    val reference: String?,
    val note: String?,
    val status: String?,
    val rejectionReason: String?
)

data class PaymentListResponse(val count: Int?, val items: List<PaymentDto>?)

data class VerifyBody(val decision: String, val reason: String? = null)

private data class ApiErrorBody(val error: String?)

object ApiErrors {
    /** The server's explanation for a failed request, e.g. "You already have a pending payment for 2026-11". */
    fun message(response: Response<*>): String? = try {
        Gson().fromJson(response.errorBody()?.string(), ApiErrorBody::class.java)?.error
    } catch (e: Exception) {
        null
    }
}