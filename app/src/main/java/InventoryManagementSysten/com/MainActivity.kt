package InventoryManagementSysten.com

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import InventoryManagementSysten.com.ui.theme.InventoryManagementSystemTheme
import org.json.JSONArray
import org.json.JSONObject

data class InventoryItem(
    val name: String,
    val quantity: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryManagementSystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    InventoryApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    var items by remember { mutableStateOf(listOf<InventoryItem>()) }
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("0") }
    
    // Load saved items when the app starts
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
        val savedItemsJson = sharedPrefs.getString("inventory_items", "[]")
        try {
            val jsonArray = JSONArray(savedItemsJson)
            val loadedItems = mutableListOf<InventoryItem>()
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                val name = itemObj.getString("name")
                val quantity = itemObj.getInt("quantity")
                loadedItems.add(InventoryItem(name, quantity))
            }
            items = loadedItems
        } catch (e: Exception) {
            items = listOf()
        }
    }
    
    // Save items whenever the items list changes
    LaunchedEffect(items) {
        val sharedPrefs = context.getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        items.forEach { item ->
            val itemObj = JSONObject()
            itemObj.put("name", item.name)
            itemObj.put("quantity", item.quantity)
            jsonArray.put(itemObj)
        }
        sharedPrefs.edit().putString("inventory_items", jsonArray.toString()).apply()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Inventory Management System",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Add new item section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add New Item",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Item name input
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                // Quantity input with plus/minus buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity:",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    IconButton(
                        onClick = {
                            val currentQuantity = itemQuantity.toIntOrNull() ?: 0
                            itemQuantity = (currentQuantity - 1).coerceAtLeast(0).toString()
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                    }
                    
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { 
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                itemQuantity = it
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            val currentQuantity = itemQuantity.toIntOrNull() ?: 0
                            itemQuantity = (currentQuantity + 1).toString()
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                    }
                }

                // Add button
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            val quantity = itemQuantity.toIntOrNull() ?: 0
                            val newItem = InventoryItem(itemName, quantity)
                            items = items + newItem
                            itemName = ""
                            itemQuantity = "0"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = itemName.isNotBlank()
                ) {
                    Text("Add Item")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Inventory list
        Text(
            text = "Current Inventory",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items in inventory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    InventoryItemCard(
                        item = item,
                        onQuantityChange = { newQuantity ->
                            items = items.map { 
                                if (it.name == item.name) it.copy(quantity = newQuantity) else it 
                            }
                        },
                        onDelete = {
                            items = items.filter { it.name != item.name }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item name
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onQuantityChange((item.quantity - 1).coerceAtLeast(0)) }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                }
                
                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) }
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryAppPreview() {
    InventoryManagementSystemTheme {
        InventoryApp()
    }
}