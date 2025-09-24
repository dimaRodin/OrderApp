package com.example.orderapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.orderapp.OrderAppTheme
import com.example.orderapp.LoginScreen
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
                    LoginScreen { server, loginText, passwordText ->
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                        prefs.edit().apply {
                            putString("server", server)
                            putString("login", loginText)
                            putString("password", passwordText)
                            apply()
                        }
                        startActivity(Intent(this, OrdersListActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}