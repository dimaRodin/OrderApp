package com.example.orderapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Указывает на layout для этой активности

        // Логирование для отладки
        Log.d("LoginActivity", "onCreate called")

        // Инициализация полей из layout
        val serverAddress = findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.server_address)
        val login = findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.login)
        val password = findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.password)
        val enterButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.enter_button)

        // Обработчик кнопки входа
        enterButton.setOnClickListener {
            val server = serverAddress.text.toString().trim()
            val loginText = login.text.toString().trim()
            val passwordText = password.text.toString()

            if (server.isEmpty() || loginText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Сохранение данных в SharedPreferences
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit().apply {
                putString("server", server)
                putString("login", loginText)
                putString("password", passwordText)
                apply()
            }

            // Переход к следующей активности (замените на вашу следующую активность)
            startActivity(Intent(this, OrdersListActivity::class.java))
            finish() // Закрываем LoginActivity после перехода
        }
    }
}