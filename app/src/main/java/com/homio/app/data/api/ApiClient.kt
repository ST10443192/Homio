package com.homio.app.data.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.homio.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Adds the signed-in user's Firebase login token to every request. */
private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val user = FirebaseAuth.getInstance().currentUser ?: return chain.proceed(request)

        val token = try {
            // false = reuse the saved token, only fetch a new one when it has expired
            Tasks.await(user.getIdToken(false), 15, TimeUnit.SECONDS).token
        } catch (e: Exception) {
            null
        }

        val signed = if (token != null) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else request
        return chain.proceed(signed)
    }
}

object ApiClient {

    val service: HomioApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("Authorization")   // never log the login token
                        }
                    )
                }
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HomioApi::class.java)
    }
}