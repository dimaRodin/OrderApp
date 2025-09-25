package com.example.orderapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.preference.PreferenceManager
import android.content.Intent

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OrderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen { server, loginText, passwordText, store ->
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                        prefs.edit().apply {
                            putString("server", server)
                            putString("login", loginText)
                            putString("password", passwordText)
                            putString("store", store) // Сохраняем магазин
                            apply()
                        }
                        val intent = Intent(this, OrdersListActivity::class.java)
                        intent.putExtra("store_id", store) // Передаем магазин в OrdersListActivity
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}