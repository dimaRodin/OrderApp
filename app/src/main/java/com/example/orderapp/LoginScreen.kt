package com.example.orderapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLogin: (server: String, login: String, password: String, store: String) -> Unit) {

    var server by remember { mutableStateOf("http://acc.konark.com.ua:8080/") }
    var loginText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val stores = mapOf(
        "U001" to "U001 Гетьмана",
        "U002" to "U002 Монастирського",
        "U004" to "U004 Берестейська",
        "U005" to "U005 Глибочицька"
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedStore by remember { mutableStateOf("U001") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = server,
            onValueChange = { server = it },
            label = { Text("Адреса серверу") }
        )
        OutlinedTextField(
            value = loginText,
            onValueChange = { loginText = it },
            label = { Text("Логін") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = stores[selectedStore] ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Магазин") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                stores.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedStore = code
                            expanded = false
                        }
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (server.isNotBlank() && loginText.isNotBlank() && password.isNotBlank()) {
                onLogin(server, loginText, password, selectedStore)
            }
        }) {
            Text("Вхід")
        }
    }
}