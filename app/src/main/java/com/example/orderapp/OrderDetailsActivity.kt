package com.example.orderapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import android.provider.MediaStore
import android.app.Activity
import android.graphics.Bitmap
import com.example.orderapp.ItemsAdapter
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import com.example.orderapp.RetrofitClient


class OrderDetailsActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ORDER = "order"
        private const val REQUEST_CAMERA = 1001

        fun newIntent(context: Context, order: Order): Intent {
            return Intent(context, OrderDetailsActivity::class.java).apply {
                putExtra(EXTRA_ORDER, order) // Make Order Serializable or Parcelable
            }
        }
    }

    private lateinit var order: Order
    private lateinit var itemsRecycler: RecyclerView
    private lateinit var searchEdit: EditText
    private lateinit var scanButton: Button
    private lateinit var completeButton: Button
    private lateinit var partialButton: Button
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        order = intent.getSerializableExtra(EXTRA_ORDER) as Order // Assume Serializable

        itemsRecycler = findViewById(R.id.items_recycler)
        itemsRecycler.layoutManager = LinearLayoutManager(this)
        itemsRecycler.adapter = ItemsAdapter(order.items) { item ->
            // Show dialog to edit qty
            showQtyDialog(item)
        }

        searchEdit = findViewById(R.id.search_edit)
        scanButton = findViewById(R.id.scan_button)
        completeButton = findViewById(R.id.complete_button)
        partialButton = findViewById(R.id.partial_button)
        saveButton = findViewById(R.id.save_button)

        scanButton.setOnClickListener {
            // Start camera for capture
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_CAMERA)
        }

        searchEdit.setOnEditorActionListener { _, _, _ ->
            searchItem(searchEdit.text.toString())
            true
        }

        var status = ""
        completeButton.setOnClickListener {
            status = "complete"
            // Highlight button
        }
        partialButton.setOnClickListener {
            status = "partial"
            // Highlight button
        }

        saveButton.setOnClickListener {
            if (status.isEmpty()) {
                Toast.makeText(this, "Select status", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val request = RecordRequest(order.id, order.items, status)
            RetrofitClient.getApiService(this).recordOrder(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@OrderDetailsActivity, "Saved", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@OrderDetailsActivity, "Error saving", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@OrderDetailsActivity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            val image = InputImage.fromBitmap(bitmap, 0)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val value = barcode.rawValue
                        if (value != null) {
                            searchItem(value)
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun searchItem(query: String) {
        val item = order.items.find { it.barcode == query || it.name.contains(query, ignoreCase = true) }
        if (item != null) {
            showQtyDialog(item)
        } else {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showQtyDialog(item: Item) {
        // Use AlertDialog with EditText for qty
        val dialogView = layoutInflater.inflate(R.layout.dialog_qty, null)
        val qtyEdit = dialogView.findViewById<EditText>(R.id.qty_edit)
        qtyEdit.setText(item.collected_qty.toString())

        android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                item.collected_qty = qtyEdit.text.toString().toDoubleOrNull() ?: 0.0
                itemsRecycler.adapter?.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}