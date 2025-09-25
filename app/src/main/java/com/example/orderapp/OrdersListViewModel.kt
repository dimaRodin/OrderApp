package com.example.orderapp

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersListViewModel : ViewModel() {
    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchOrders(context: Context, storeId: String) {
        RetrofitClient.getApiService(context).getOrders(storeId).enqueue(object : Callback<OrdersResponse> {
            override fun onResponse(call: Call<OrdersResponse>, response: Response<OrdersResponse>) {
                if (response.isSuccessful) {
                    orders = response.body()?.orders ?: emptyList()
                } else {
                    Toast.makeText(context, "Error fetching orders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                Toast.makeText(context, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}