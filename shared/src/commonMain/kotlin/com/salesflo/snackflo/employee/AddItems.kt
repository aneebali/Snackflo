package com.salesflo.snackflo.employee

import AppPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import com.salesflo.snackflo.repository.EmployeeViewModel
import com.salesflo.snackflo.repository.Item
import com.salesflo.snackflo.repository.RestaurantViewModel
import com.salesflo.snackflo.repository.RestaurantWithItems
import com.salesflo.snackflo.repository.SelectedOrderItems
import com.salesflo.snackflo.showToast
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantExpansionScreen(
    viewModel: RestaurantViewModel = RestaurantViewModel(),
    onArrowClick: () -> Unit,employeeViewModel: EmployeeViewModel = viewModel {EmployeeViewModel()}
) {
    val restaurantsWithItems by viewModel.getRestaurantsWithItems()
        .collectAsState(initial = emptyList())
    val uiState by viewModel.uiState
    var isSubmitting by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateMapOf<String, SelectedOrderItems>() }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Restaurants & Items") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF7F50),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {

                        if (isSubmitting) return@FloatingActionButton
                        isSubmitting = true

                        employeeViewModel.submitSelectedOrder(
                            selectedItems.values.toList(),
                            onSuccess = {
                                showToast("Order placed successfully!")
                                selectedItems.clear()
                                isSubmitting = false
                                onArrowClick()
                            },
                            onFailure = {
                                showToast("Failed to place order. Try again.")
                                isSubmitting = false
                            }
                        )
                    },

                    containerColor = if (isSubmitting) Color.Gray else Color(0xFFFF7F50),

                    ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Submit Order",
                            tint = Color.White
                        )
                    }
                }

            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },

                placeholder = { Text("search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    disabledContainerColor = Color(0xFFF2F2F2),

                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,

                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    //  placeholderColor = Color.Gray,
                    focusedLeadingIconColor = Color.Gray,
                    unfocusedLeadingIconColor = Color.Gray
                )
            )

            val filteredList = if (searchQuery.isBlank()) {
                restaurantsWithItems
            } else {
                restaurantsWithItems.mapNotNull { restaurant ->
                    val matchesRestaurant =
                        restaurant.restaurant.name.contains(searchQuery, ignoreCase = true)
                    val matchingItems = restaurant.items.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    if (matchesRestaurant || matchingItems.isNotEmpty()) {
                        restaurant.copy(items = if (matchesRestaurant) restaurant.items else matchingItems)
                    } else {
                        null
                    }
                }
            }

            RestaurantExpansionCards(
                restaurantsWithItems = filteredList,
                modifier = Modifier.fillMaxSize(),
                onItemSelected = { selectedItem ->
                    if (selectedItem != null && selectedItem.quantity != "0") {
                        selectedItems[selectedItem.Itemid] = selectedItem
                    } else if (selectedItem != null) {
                        selectedItems.remove(selectedItem.Itemid)
                    }
                },

                selectedItems = selectedItems
            )

            if (uiState.isLoading && restaurantsWithItems.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.refreshData() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantExpansionCards(
    restaurantsWithItems: List<RestaurantWithItems>,
    modifier: Modifier = Modifier,
    onItemSelected: (SelectedOrderItems?) -> Unit,
    selectedItems: Map<String, SelectedOrderItems>
) {
    var expandedCardIds by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = restaurantsWithItems,
            key = { it.restaurant.id }
        ) { restaurantWithItems ->
            RestaurantCard(
                restaurantWithItems = restaurantWithItems,
                isExpanded = expandedCardIds.contains(restaurantWithItems.restaurant.id),
                onExpandToggle = { id ->
                    expandedCardIds = if (expandedCardIds.contains(id)) {
                        expandedCardIds - id
                    } else {
                        expandedCardIds + id
                    }
                },
                onItemSelected = onItemSelected,
                selectedItems = selectedItems
            )
            Spacer(modifier = Modifier.height(4.dp))


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCard(
    restaurantWithItems: RestaurantWithItems,
    isExpanded: Boolean,
    onExpandToggle: (Int) -> Unit,
    onItemSelected: (SelectedOrderItems?) -> Unit,
    selectedItems: Map<String, SelectedOrderItems>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle(restaurantWithItems.restaurant.id) }
                    .background(color = Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF7F50)),
                        contentAlignment = Alignment.Center
                    ) {


                        Text(
                            text = restaurantWithItems.restaurant.name
                                .takeIf { it.isNotEmpty() }
                                ?.first()?.toString()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = restaurantWithItems.restaurant.name.ifEmpty { "Unknown Category" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${restaurantWithItems.items.size} items available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) Color(0xFFFF7F50) else Color(0xFFF7FAFC),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                        tint = if (isExpanded) Color.White else Color(0xFFFF7F50),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    if (restaurantWithItems.items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items available for this category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        restaurantWithItems.items.forEachIndexed { index, item ->
                            val selectedOrder = selectedItems[item.id]
                            ItemCard(
                                item = item,
                                selectedOrder = selectedOrder,
                                onItemSelected = onItemSelected
                            )
                            if (index < restaurantWithItems.items.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    item: Item,
    selectedOrder: SelectedOrderItems?,
    onItemSelected: (SelectedOrderItems?) -> Unit,
    modifier: Modifier = Modifier
) {
    fun getQuantityOptionsForUnit(unit: String): List<String> {
        return when (unit.lowercase()) {
            "paao" -> listOf("0", "0.5 kg", "1 kg", "1.5 kg", "2 kg", "2.5 kg", "3 kg")
            "quantity" -> (0..10).map { it.toString() }
            "half" -> listOf("0", "Half", "Full")
            "kg" -> listOf("0", "1 paao", "1.5 paao", "0.5 kilo", "1 kilo", "1.5 kilo", "2 kilo")
            else -> listOf("0", "1", "2", "3", "4", "5")


            //BANANA SHAKE
        }
    }

    @Composable
    fun ImageWithLoading(imageUrl: String, modifier: Modifier = Modifier) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF7F50),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            error = {
                println("ImageLoad, Failed to load image: $imageUrl")

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image not available",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        )
    }


    fun LocalTime.withoutNanoseconds(): LocalTime =
        LocalTime(hour, minute, second)

    var selectedTime by remember {
        mutableStateOf(
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .time
                .withoutNanoseconds()
        )
    }

    val selectedDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var expanded by remember { mutableStateOf(false) }
    var selectedQuantity by rememberSaveable { mutableStateOf(selectedOrder?.quantity ?: "0") }

    val quantityOptions = getQuantityOptionsForUnit(item.unit)

    val id = AppPreferences.userId

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            ImageWithLoading(
                imageUrl = item.Image,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name.ifEmpty { "Unknown Item" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
//                if (item.unit.isNotBlank()) {
//                    Text(
//                        text = "Unit: ${item.unit}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
//                    )
//                }
            }
            Text(
                text = "Qty:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(1.dp))
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(selectedQuantity, style = TextStyle(color = Color.Black))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        tint = Color(0xFFFF7F50),
                        contentDescription = "Select quantity"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    quantityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedQuantity = option
                                expanded = false

                                val quantityValue = option

                                val order = SelectedOrderItems(
                                    Itemid = item.id,
                                    price = 0,
                                    quantity = quantityValue,
                                    note = "",
                                    date = selectedDate.toString(),
                                    time = selectedTime.toString(),
                                    empId = id,
                                    categoryId = item.categoryId
                                )

                                onItemSelected(order)
                            }
                        )
                    }
                }
            }
        }
    }
}


