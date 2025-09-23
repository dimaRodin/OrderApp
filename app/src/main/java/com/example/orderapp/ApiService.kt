package com.example.orderapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.Serializable

interface ApiService {
    @GET("konark_intelmed_erp/hs/zak/get_zak")
    fun getOrders(): Call<OrdersResponse>

    @POST("konark_intelmed_erp/hs/zak/record_zak")
    fun recordOrder(@Body request: RecordRequest): Call<Void>
}

// Модели данных
data class OrdersResponse(val orders: List<Order>)
data class Order(val id: String, val date: String, val items: List<Item>) : Serializable
data class Item(val name: String, val barcode: String, val planned_qty: Double, val price: Double, var collected_qty: Double = 0.0) : Serializable

data class RecordRequest(
    val order_id: String,
    val items: List<Item>,
    val status: String
)