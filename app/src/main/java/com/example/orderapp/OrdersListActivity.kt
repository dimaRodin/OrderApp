package com.example.orderapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class OrdersListActivity : ComponentActivity() {

    private val viewModel: OrdersListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storeId = intent.getStringExtra("store_id")

        setContent {
            OrderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OrdersListScreen(viewModel)
                }
            }
        }

        if (storeId != null) {
            viewModel.fetchOrders(this, storeId)
        }
    }
}

@Composable
fun OrdersListScreen(viewModel: OrdersListViewModel) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewModel.orders) { order ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, OrderDetailsActivity::class.java)
                        intent.putExtra("order", order)
                        context.startActivity(intent)
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Order ID: ${order.id}")
                    Text(text = "Date: ${order.date}")
                }
            }
        }
    }
}