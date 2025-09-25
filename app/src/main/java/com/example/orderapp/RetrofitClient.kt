package com.example.orderapp

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getApiService(context: Context): ApiService {
        if (retrofit == null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val server = prefs.getString("server", "") ?: ""
            val login = prefs.getString("login", "") ?: ""
            val password = prefs.getString("password", "") ?: ""

            val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("Authorization", Credentials.basic(login, password))
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(server)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}