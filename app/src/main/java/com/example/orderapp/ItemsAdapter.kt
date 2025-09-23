package com.example.orderapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemsAdapter(private val items: List<Item>, private val onClick: (Item) -> Unit) :
    RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.item_name)
        val plannedText: TextView = view.findViewById(R.id.planned_qty)
        val collectedText: TextView = view.findViewById(R.id.collected_qty)
        val priceText: TextView = view.findViewById(R.id.price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameText.text = item.name
        holder.plannedText.text = item.planned_qty.toString()
        holder.collectedText.text = item.collected_qty.toString()
        holder.priceText.text = "${item.price} грн"

        // Цветовая индикация для collected_qty
        holder.collectedText.setTextColor(when {
            item.collected_qty < item.planned_qty -> 0xFFFF0000.toInt() // Красный
            item.collected_qty == item.planned_qty -> 0xFF00FF00.toInt() // Зеленый
            else -> 0xFFFFFF00.toInt() // Желтый
        })

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}