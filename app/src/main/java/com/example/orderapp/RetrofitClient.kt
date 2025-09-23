package com.example.orderapp

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getApiService(context: Context): ApiService {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val server = prefs.getString("server", "http://acc.konark.com.ua:8080/") ?: "http://acc.konark.com.ua:8080/"
        val login = prefs.getString("login", "") ?: ""
        val password = prefs.getString("password", "") ?: ""

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", Credentials.basic(login, password))
                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(server)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}