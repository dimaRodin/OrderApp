package com.example.orderapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable // <--- ДОБАВЛЕН ИМПОРТ
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.orderapp.OrderAppTheme
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val order = intent.getSerializableExtra("order") as Order
        setContent {
            OrderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OrderDetailsScreen(order = order)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(order: Order) {
    var status by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val items = remember { mutableStateListOf(*order.items.toTypedArray()) }
    var showQtyDialog by remember { mutableStateOf<Item?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
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
                            val item = items.find { it.barcode == value }
                            if (item != null) {
                                showQtyDialog = item
                            } else {
                                Toast.makeText(context, "Товар не найден", Toast.LENGTH_SHORT).show()
                            }
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Ошибка сканирования: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заказа") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Поиск или сканирование") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        val item = items.find { it.barcode == searchText || it.name.contains(searchText, ignoreCase = true) }
                        if (item != null) {
                            showQtyDialog = item
                        } else {
                            Toast.makeText(context, "Товар не найден", Toast.LENGTH_SHORT).show()
                        }
                    })
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(intent)
                }) {
                    Text("Скан")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    ItemRow(item = item, onClick = { showQtyDialog = item })
                    Divider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { status = "complete" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "complete") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("Выполнен")
                }
                Button(
                    onClick = { status = "partial" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "partial") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("Частично")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (status.isEmpty()) {
                        Toast.makeText(context, "Выберите статус", Toast.LENGTH_SHORT).show()
                    } else {
                        val request = RecordRequest(order.id, items, status)
                        RetrofitClient.getApiService(context).recordOrder(request).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Сохранено", Toast.LENGTH_SHORT).show()
                                    (context as? Activity)?.finish()
                                } else {
                                    Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }

    if (showQtyDialog != null) {
        QtyDialog(
            item = showQtyDialog!!,
            onDismiss = { showQtyDialog = null },
            onConfirm = { newItem ->
                val index = items.indexOfFirst { it.barcode == newItem.barcode }
                if (index != -1) {
                    items[index] = newItem
                }
                showQtyDialog = null
            }
        )
    }
}

@Composable
fun ItemRow(item: Item, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text("Штрих-код: ${item.barcode}", style = MaterialTheme.typography.bodySmall)
        }
        // <--- ИЗМЕНЕНА СТРОКА НИЖЕ
        Text("Кол-во: ${item.collected_qty}", style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QtyDialog(item: Item, onDismiss: () -> Unit, onConfirm: (Item) -> Unit) {
    var qty by remember { mutableStateOf(item.collected_qty.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Введите количество") },
        text = {
            OutlinedTextField(
                value = qty,
                onValueChange = { qty = it },
                label = { Text("Собранное количество") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(item.copy(collected_qty = qty.toDoubleOrNull() ?: 0.0))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}