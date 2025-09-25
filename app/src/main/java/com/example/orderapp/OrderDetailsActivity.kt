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
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.orderapp.OrderAppTheme
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class OrderDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val order = intent.getSerializableExtra("order") as? Order

        if (order == null) {
            Toast.makeText(this, "Ошибка: не удалось загрузить заказ.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            OrderAppTheme {
                OrderDetailsScreen(initialOrder = order)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    initialOrder: Order,
    viewModel: OrderDetailsViewModel = viewModel()
) {
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val items = viewModel.items
    var showQtyDialog by remember { mutableStateOf<Item?>(null) }

    LaunchedEffect(initialOrder) {
        viewModel.initializeOrder(initialOrder)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                BarcodeScanning.getClient(options).process(image)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { value ->
                            val item = items.find { it.barcode == value }
                            if (item != null) {
                                showQtyDialog = item
                            } else {
                                Toast.makeText(context, "Товар не найден", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Ошибка сканирования", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Детали заказа №${initialOrder.id}") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Поиск или сканирование") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        val item = items.find { it.barcode == searchText || it.name.contains(searchText, ignoreCase = true) }
                        if (item != null) showQtyDialog = item
                        else Toast.makeText(context, "Товар не найден", Toast.LENGTH_SHORT).show()
                    })
                )
                Button(onClick = { cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE)) }) {
                    Text("Скан")
                }
            }

            // 👇 ИЗМЕНЕНИЕ ЗДЕСЬ: УДАЛЕН ПАРАМЕТР key
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    ItemRow(item = item, onClick = { showQtyDialog = item })
                    Divider()
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.status = "complete" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.status == "complete") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("Выполнен") }
                Button(
                    onClick = { viewModel.status = "partial" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.status == "partial") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("Частично") }
            }
            Button(
                onClick = {
                    val activity = (context as? Activity)
                    viewModel.saveOrder(context) {
                        activity?.finish()
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
            onConfirm = { updatedItem: Item ->
                viewModel.updateItemQty(updatedItem, updatedItem.collected_qty)
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text("Штрих-код: ${item.barcode}", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "Кол-во: ${item.collected_qty} / ${item.collected_qty}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QtyDialog(item: Item, onDismiss: () -> Unit, onConfirm: (Item) -> Unit) {
    var qty by remember { mutableStateOf(item.collected_qty.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            OutlinedTextField(
                value = qty,
                onValueChange = { qty = it },
                label = { Text("Собранное количество (из ${item.collected_qty})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                val newQty = qty.toDoubleOrNull() ?: item.collected_qty
                onConfirm(item.copy(collected_qty = newQty))
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