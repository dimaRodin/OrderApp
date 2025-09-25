package com.example.orderapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orderapp.OrderAppTheme

class OrdersListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrderAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OrdersListScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(viewModel: OrdersListViewModel = viewModel()) {
    val context = LocalContext.current
    val orders = viewModel.orders

    // Запускаем загрузку данных только один раз при создании экрана
    LaunchedEffect(Unit) {
        viewModel.fetchOrders(context)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Заказы") }) }
    ) { padding ->
        // ИСПРАВЛЕНО: Проверяем viewModel.errorMessage на null
        val currentErrorMessage = viewModel.errorMessage
        if (currentErrorMessage != null) {
            // Если есть ошибка, показываем ее
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = currentErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Если ошибок нет, показываем список заказов
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(orders, key = { it.id }) { order ->
                    OrderItem(order = order, onClick = {
                        val intent = Intent(context, OrderDetailsActivity::class.java).apply {
                            putExtra("order", order)
                        }
                        context.startActivity(intent)
                    })
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Заказ #${order.id}", style = MaterialTheme.typography.titleMedium)
        Text(text = order.date, style = MaterialTheme.typography.bodyMedium)
    }
}