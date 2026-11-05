package com.homio.app.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface HomioApi {

    // ----- Maintenance -----

    @POST("api/maintenance")
    suspend fun createMaintenance(@Body body: CreateMaintenanceBody): Response<MaintenanceDto>

    @PATCH("api/maintenance/{id}/status")
    suspend fun updateMaintenanceStatus(
        @Path("id") id: String,
        @Body body: StatusBody
    ): Response<MaintenanceDto>

    // ----- Payments -----

    @Multipart
    @POST("api/payments")
    suspend fun submitPayment(
        @Part amount: MultipartBody.Part,
        @Part month: MultipartBody.Part,
        @Part reference: MultipartBody.Part,
        @Part note: MultipartBody.Part,
        @Part proof: MultipartBody.Part
    ): Response<PaymentDto>

    @GET("api/payments")
    suspend fun getPayments(@Query("status") status: String? = null): Response<PaymentListResponse>

    @GET("api/payments/{id}/proof")
    suspend fun getProof(@Path("id") id: String): Response<ResponseBody>

    @PATCH("api/payments/{id}/verify")
    suspend fun verifyPayment(
        @Path("id") id: String,
        @Body body: VerifyBody
    ): Response<PaymentDto>
}