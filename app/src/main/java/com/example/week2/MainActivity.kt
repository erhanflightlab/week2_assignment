package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.week2.ui.theme.Week2_assignmentTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Week2_assignmentTheme {
                AllocationDeskScreen()
            }
        }
    }
}

@Composable
fun AllocationDeskScreen() {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var selectedCategory by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var allocatedQuantities by rememberSaveable {
        mutableStateOf(IntArray(LabHardwareInventory.items.size))
    }

    val categories = remember {
        LabHardwareInventory.items.map { it.category }.distinct()
    }

    val inventory = remember(allocatedQuantities) {
        LabHardwareInventory.items.mapIndexed { index, hardware ->
            hardware.copy(
                stockCount = hardware.stockCount - allocatedQuantities[index]
            )
        }
    }

    val totalCost = remember(allocatedQuantities) {
        LabHardwareInventory.items.mapIndexed { index, hardware ->
            hardware.cost * allocatedQuantities[index]
        }.sum()
    }

    fun changeReservedQuantity(id: String, change: Int) {
        val index = LabHardwareInventory.items.indexOfFirst { it.id == id }
        if (index == -1) return

        val newQuantity = allocatedQuantities[index] + change
        val initialStock = LabHardwareInventory.items[index].stockCount

        if (newQuantity !in 0..initialStock) return

        val updatedQuantities = allocatedQuantities.copyOf()
        updatedQuantities[index] = newQuantity
        allocatedQuantities = updatedQuantities
    }

    val filteredItems = remember(searchQuery, selectedCategory) {
        val query = searchQuery.trim()

        LabHardwareInventory.items.filter { hardware ->
            val matchesSearch =
                hardware.name.contains(query, ignoreCase = true) ||
                        hardware.category.contains(query, ignoreCase = true) ||
                        hardware.id.contains(query)

            val matchesCategory =
                selectedCategory == null ||
                        hardware.category == selectedCategory

            matchesSearch && matchesCategory
        }
    }
    val compactHeader = LocalConfiguration.current.screenHeightDp < 480
    Scaffold(
        containerColor = Color(0xFFF3F5F8)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AllocationSummaryPanel(
                    hardwareTypeCount = LabHardwareInventory.items.size,
                    allocatedCount = allocatedQuantities.sum(),
                    totalCost = totalCost,
                    compact = compactHeader
                )
            }

            item {
                InventoryCategoryStrip(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            item {
                HardwareSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            item {
                Text(
                    text = "${selectedCategory ?: "Tüm donanımlar"} · ${filteredItems.size} sonuç",
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF172B46)
                )
            }

            if (filteredItems.isEmpty()) {
                item {
                    Text(
                        text = "Eşleşen donanım bulunamadı. Başka bir ad veya kategori deneyin.",
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF65758B)
                    )
                }
            }

            items(
                items = filteredItems,
                key = { it.id }
            ) { hardware ->
                val catalogIndex = LabHardwareInventory.items.indexOfFirst {
                    it.id == hardware.id
                }

                HardwareCard(
                    item = inventory[catalogIndex],
                    allocatedCount = allocatedQuantities[catalogIndex],
                    onAllocate = { changeReservedQuantity(hardware.id, 1) },
                    onRelease = { changeReservedQuantity(hardware.id, -1) }                )
            }
        }
    }
}

@Composable
fun HardwareCard(
    item: HardwareItem,
    allocatedCount: Int,
    onAllocate: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockColor = if (item.stockCount > 0) {
        Color(0xFF216B50)
    } else {
        Color(0xFFB33E3E)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF172B46)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = item.stockCount > 0,
                    onClickLabel = "Bir adet tahsis et",
                    onClick = onAllocate
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ENV-${item.id}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF65758B)
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF65758B)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(
                        Locale.forLanguageTag("tr-TR"),
                        "%,.2f",
                        item.cost
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (item.stockCount > 0) {
                        "Kalan stok: ${item.stockCount}"
                    } else {
                        "Stok tükendi"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = stockColor
                )
            }
            Text(
                text = "Tahsis edilen: $allocatedCount adet",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF65758B)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRelease,
                    enabled = allocatedCount > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Geri al")
                }
                Button(
                    onClick = onAllocate,
                    enabled = item.stockCount > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF172B46)
                    )
                ) {
                    Text("Tahsis et")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllocationDeskPreview() {
    Week2_assignmentTheme {
        AllocationDeskScreen()
    }
}
@Composable
fun HardwareSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Donanım ara") },
        placeholder = { Text("Ad, kategori veya kimlik") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun InventoryCategoryStrip(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Tümü") }
            )
        }

        items(
            items = categories,
            key = { it }
        ) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}
@Composable
fun AllocationSummaryPanel(
    hardwareTypeCount: Int,
    allocatedCount: Int,
    totalCost: Double,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF172B46),
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(
                if (compact) 12.dp else 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(
                if (compact) 4.dp else 8.dp
            )
        ) {
            Text(
                text = "DevCluster",
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold
            )

            if (!compact) {
                Text(
                    text = "Donanım ve kaynak yönetimi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD0DAE8)
                )
            }

            Text(
                text = "$hardwareTypeCount donanım türü · " +
                        "$allocatedCount adet tahsis",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFD0DAE8)
            )

            Text(
                text = "Toplam maliyet: " + String.format(
                    Locale.forLanguageTag("tr-TR"),
                    "%,.2f",
                    totalCost
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}