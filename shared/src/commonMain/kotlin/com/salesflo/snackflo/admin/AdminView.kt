package com.salesflo.snackflo.admin


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesflo.snackflo.repository.EmployeeOrders
import com.salesflo.snackflo.repository.OrderViewModel
import com.salesflo.snackflo.repository.Restaurant
import com.salesflo.snackflo.repository.RestaurantOrderData
import com.salesflo.snackflo.repository.SelectedOrderItems
import com.salesflo.snackflo.repository.groupOrdersByRestaurantAndEmployee
import com.salesflo.snackflo.repository.submitPrices
import com.salesflo.snackflo.showToast
import com.salesflo.snackflo.repository.Item
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun OrderManagementScreen(
    viewModel: OrderViewModel = viewModel{OrderViewModel()},
    selectedDate: LocalDate
) {
    val orderItems by viewModel.orderItems.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val items by viewModel.items.collectAsState()
    val employeeNames by viewModel.employeeNames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDate) {
        viewModel.setupRealtimeListeners(selectedDate)
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF7F50))
        }
    }
    else if(orderItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No orders found",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Orders for this date will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center
            )
        }
    }
    else {
        RestaurantOrderScreen(
            orderItems = orderItems,
            restaurants = restaurants,
            items = items,
            employeeNames = employeeNames,
            onSubmitPrices = { pricesMap, restaurantOrderData ->
                scope.launch {
                    submitPrices(pricesMap, restaurantOrderData)
                }
            }
        )
    }
}

@Composable
fun RestaurantOrderScreen(
    orderItems: List<SelectedOrderItems>,
    restaurants: List<Restaurant>,
    items: List<Item>,
    employeeNames: Map<String, String> = emptyMap(),
    onQuantityUpdate: (String, String) -> Unit = { _, _ -> },
    onSubmitPrices: (Map<String, Int>, List<RestaurantOrderData>) -> Unit,
) {
    var expandedRestaurants by remember { mutableStateOf(setOf<Int>()) }
    var expandedEmployees by remember { mutableStateOf(setOf<String>()) }

    val prices = remember {
        mutableStateMapOf<String, String>().apply {
            orderItems.forEach { item ->
                if (!containsKey(item.orderId)) {
                    this[item.orderId] = item.price?.toString() ?: "0"
                }
            }
        }
    }

    var quantities by remember { mutableStateOf(mutableMapOf<String, String>()) }


    var submitted by remember { mutableStateOf(false) }


    LaunchedEffect(prices.values.toList()) {
        if (submitted) {
            submitted = false
        }
    }

    val focusManager = LocalFocusManager.current

    val restaurantOrderData = remember(orderItems, restaurants, employeeNames) {
        groupOrdersByRestaurantAndEmployee(orderItems, restaurants, items, employeeNames)
    }
//    val allPricesFilled = restaurantOrderData.all { restaurant ->
//        restaurant.employeeOrders.all { emp ->
//            emp.items.all { item ->
//                val price = prices[item.orderId]?.toIntOrNull()
//                price != null && price >= 0
//            }
//        }
//    }

    val allPricesFilled = restaurantOrderData.all { restaurant ->
        restaurant.employeeOrders.all { emp ->
            emp.items.all { item ->
                val price = prices[item.orderId]?.toIntOrNull()
                price != null && price > 0
            }
        }
    }
    val anyPriceGreaterThanZero = prices.values.any {
        val price = it.toIntOrNull() ?: 0
        price > 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            items(
                count = restaurantOrderData.size,
                key = { index -> restaurantOrderData[index].restaurant.id }
            ) { index ->
                val restaurantData = restaurantOrderData[index]
                RestaurantCard(
                    restaurantData = restaurantData,
                    isExpanded = expandedRestaurants.contains(restaurantData.restaurant.id),
                    onExpandToggle = { restaurantId ->
                        expandedRestaurants = if (expandedRestaurants.contains(restaurantId)) {
                            expandedRestaurants - restaurantId
                        } else {
                            expandedRestaurants + restaurantId
                        }
                    },
                    expandedEmployees = expandedEmployees,
                    onEmployeeExpandToggle = { empKey ->
                        expandedEmployees = if (expandedEmployees.contains(empKey)) {
                            expandedEmployees - empKey
                        } else {
                            expandedEmployees + empKey
                        }
                    },
                    quantities = quantities,
                    prices = prices,
                    onPriceChange = { orderId, value ->
                        prices[orderId] = value
                        // Reset submitted state when any price is changed
                        submitted = false
                    }
                )
            }
        }

        if (allPricesFilled && !submitted) {
            Button(
                onClick = {
                    val intPrices = prices.map { (k, v) ->
                        val finalPrice = if (v.isBlank()) 0 else v.toInt()
                        k to finalPrice
                    }.toMap()

                    onSubmitPrices(intPrices, restaurantOrderData)
                    showToast("Prices submitted successfully")
                    submitted = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 10.dp, bottom = 30.dp, start = 10.dp, end = 10.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7F50))
            ) {
                Text("Submit Price")
            }
        }


//        if (allPricesFilled && !submitted) {
//            Button(
//                onClick = {
//                    val intPrices = prices.mapNotNull { (k, v) ->
//                        v.toIntOrNull()?.let { k to it }
//                    }.toMap()
//                    onSubmitPrices(intPrices, restaurantOrderData)
//                    showToast("Prices submitted successfully")
//                    submitted = true
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(top = 10.dp, bottom = 30.dp, start = 10.dp, end = 10.dp)
//                    .height(50.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7F50))
//            ) {
//                Text("Submit Price")
//            }
//        }

//        if (allPricesFilled && anyPriceGreaterThanZero && !submitted) {
//            Button(
//                onClick = {
//                    val intPrices = prices.mapNotNull { (k, v) ->
//                        v.toIntOrNull()?.let { k to it }
//                    }.toMap()
//                    onSubmitPrices(intPrices, restaurantOrderData)
//                    showToast("Prices submitted successfully")
//                    submitted = true
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(top = 10.dp, bottom = 30.dp, start = 10.dp, end = 10.dp)
//                    .height(50.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7F50))
//            ) {
//                Text("Submit Price")
//            }
//        }
    }
}

@Composable
fun RestaurantCard(
    restaurantData: RestaurantOrderData,
    isExpanded: Boolean,
    onExpandToggle: (Int) -> Unit,
    expandedEmployees: Set<String>,
    onEmployeeExpandToggle: (String) -> Unit,
    prices: Map<String, String>,
    onPriceChange: (String, String) -> Unit,
    quantities: Map<String, String>,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onExpandToggle(restaurantData.restaurant.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = restaurantData.restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
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

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                restaurantData.employeeOrders.forEach { employeeOrder ->
                    EmployeeOrderSection(
                        employeeOrder = employeeOrder,
                        restaurantId = restaurantData.restaurant.id,
                        isExpanded = expandedEmployees.contains("${restaurantData.restaurant.id}_${employeeOrder.empId}"),
                        onExpandToggle = onEmployeeExpandToggle,
                        prices = prices,
                        onPriceChange = onPriceChange,
                        quantities = quantities,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EmployeeOrderSection(
    employeeOrder: EmployeeOrders,
    restaurantId: Int,
    isExpanded: Boolean,
    onExpandToggle: (String) -> Unit,
    prices: Map<String, String>,
    onPriceChange: (String, String) -> Unit,
    quantities: Map<String, String>,
) {
    val empKey = "${restaurantId}_${employeeOrder.empId}"

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle(empKey) }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(employeeOrder.empName, fontWeight = FontWeight.Medium)
                Text("Total Items: ${employeeOrder.items.size}", style = MaterialTheme.typography.bodySmall)
            }
        }

        employeeOrder.items.forEach { orderItem ->
            ItemRow(
                orderItem = orderItem,
                empKey = empKey,
                prices = prices,
                onPriceChange = onPriceChange,
                quantities = quantities,
            )
        }
    }
}

@Composable
fun ItemRow(
    orderItem: SelectedOrderItems,
    empKey: String,
    quantities: Map<String, String>,
    prices: Map<String, String>,
    onPriceChange: (String, String) -> Unit
) {
    val currentPrice = prices[orderItem.orderId] ?: "0"
    val currentQuantity = quantities[orderItem.orderId] ?: orderItem.quantity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = orderItem.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Qty: $currentQuantity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (currentPrice != "0" && currentPrice.isNotEmpty())
                            Color(0xFFFF7F50) else Color(0xFFE5E7EB)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Rs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                        BasicTextField(
                            value = currentPrice,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                onPriceChange(orderItem.orderId, filtered)
                            },
                            textStyle = TextStyle(
                                textAlign = TextAlign.End,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A1A1A)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .width(70.dp)
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            decorationBox = { innerTextField ->
                                if (currentPrice.isEmpty()) {
                                    Text(
                                        text = "",
                                        style = TextStyle(
                                            textAlign = TextAlign.End,
                                            fontSize = 16.sp,
                                            color = Color(0xFFCCCCCC)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                innerTextField()
                            }
                        )


//                        BasicTextField(
//                            value = currentPrice,
//                            onValueChange = { newValue ->
//                                val filtered = newValue.filter { it.isDigit() }
//                                if (filtered != newValue) return@BasicTextField
//
//                                // Allow empty string or valid numbers
//                                val finalValue = if (filtered.isEmpty()) "0" else filtered
//                                println("TextField changed - OrderId: ${orderItem.orderId}, NewValue: $finalValue")
//                                onPriceChange(orderItem.orderId, finalValue)
//                            },
//                            textStyle = TextStyle(
//                                textAlign = TextAlign.End,
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF1A1A1A)
//                            ),
//                            keyboardOptions = KeyboardOptions(
//                                keyboardType = KeyboardType.Number,
//                                imeAction = ImeAction.Done
//                            ),
//                            singleLine = true,
//                            modifier = Modifier
//                                .width(70.dp)
//                                .padding(vertical = 12.dp, horizontal = 4.dp),
//                            decorationBox = { innerTextField ->
//                                if (currentPrice == "0") {
//                                    Text(
//                                        text = "0",
//                                        style = TextStyle(
//                                            textAlign = TextAlign.End,
//                                            fontSize = 16.sp,
//                                            color = Color(0xFFCCCCCC)
//                                        ),
//                                        modifier = Modifier.fillMaxWidth()
//                                    )
//                                }
//                                innerTextField()
//                            }
//                        )
                    }
                }
            }
        }
    }
}
