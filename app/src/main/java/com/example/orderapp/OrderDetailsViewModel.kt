package com.example.orderapp

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderDetailsViewModel : ViewModel() {
    var order by mutableStateOf<Order?>(null)
        private set

    val items = mutableStateListOf<Item>()
    var status by mutableStateOf("")

    fun initializeOrder(initialOrder: Order) {
        order = initialOrder
        items.clear()
        items.addAll(initialOrder.items)
    }

    fun updateItemQty(itemToUpdate: Item, newQty: Double) {
        val index = items.indexOfFirst { it.barcode == itemToUpdate.barcode }
        if (index != -1) {
            // ИСПРАВЛЕНО: используется collected_qty
            val updatedItem = items[index].copy(collected_qty = newQty)
            items[index] = updatedItem
        }
    }

    fun saveOrder(context: Context, onSaveFinished: () -> Unit) {
        val currentOrder = order ?: return

        if (status.isEmpty()) {
            Toast.makeText(context, "Выберите статус", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RecordRequest(currentOrder.id, items.toList(), status)
        RetrofitClient.getApiService(context).recordOrder(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                    onSaveFinished()
                } else {
                    Toast.makeText(context, "Ошибка сохранения: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}