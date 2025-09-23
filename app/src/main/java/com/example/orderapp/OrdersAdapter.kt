package com.example.orderapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(private val orders: List<Order>, private val onClick: (Order) -> Unit) :
    RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idText: TextView = view.findViewById(R.id.order_id)
        val dateText: TextView = view.findViewById(R.id.order_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.idText.text = order.id
        holder.dateText.text = order.date
        holder.itemView.setOnClickListener { onClick(order) }
    }

    override fun getItemCount(): Int = orders.size
}