package com.example.orderapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orderapp.Order
import com.example.orderapp.OrdersAdapter
import com.example.orderapp.OrdersResponse
import com.example.orderapp.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.orderapp.R

class OrdersListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var orders: List<Order> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_list)

        recyclerView = findViewById(R.id.orders_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchOrders()
    }

    private fun fetchOrders() {
        RetrofitClient.getApiService(this).getOrders().enqueue(object : Callback<OrdersResponse> {
            override fun onResponse(call: Call<OrdersResponse>, response: Response<OrdersResponse>) {
                if (response.isSuccessful) {
                    orders = response.body()?.orders ?: emptyList()
                    recyclerView.adapter = OrdersAdapter(orders) { order ->
                        startActivity(OrderDetailsActivity.newIntent(this@OrdersListActivity, order))
                    }
                } else {
                    Toast.makeText(this@OrdersListActivity, "Error fetching orders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                Toast.makeText(this@OrdersListActivity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}