package com.salesflo.snackflo.admin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesflo.snackflo.DatePickerDemo
import com.salesflo.snackflo.repository.AdminViewModel
import com.salesflo.snackflo.repository.EmployeeOrder
import com.salesflo.snackflo.repository.FoodOrder
import com.salesflo.snackflo.repository.formatDateKMP
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Composable
fun ExpandableFoodCard1(foodOrder: FoodOrder, viewModel: AdminViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
//            colors = CardDefaults.cardColors(
//                containerColor = Color(0xFFFFc0a5)
//            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // or 0.dp to remove it completely
        )


    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(foodOrder.foodName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                foodOrder.orders.forEach { order ->
                    EmployeeOrderCard1(order, viewModel = viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EmployeeOrderCard1(order: EmployeeOrder, viewModel: AdminViewModel) {
    var price by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }
    var reason by remember { mutableStateOf("Select Reason") }

    val reasonOptions = listOf("Out of stock", "Invalid order", "Other")
    val statusOptions = listOf("Approved", "Pending", "Rejected")


    var isReasonMenuExpanded by remember { mutableStateOf(false) }
    var isStatusMenuExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(order) {
        price = order.price.toString() ?: ""
        status = order.status ?: "Pending"
        reason = order.rejectReason ?: "Select Reason"
    }



    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Employee: ${order.employeeName}", fontWeight = FontWeight.Bold)
            Text("Quantity: ${order.quantity}")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Status",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box {
                Text(
                    text = status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isStatusMenuExpanded = true }
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                DropdownMenu(
                    expanded = isStatusMenuExpanded,
                    onDismissRequest = { isStatusMenuExpanded = false }
                ) {
                    statusOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                isStatusMenuExpanded = false

                            }
                        )
                    }
                }
            }

            if (status == "Rejected") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reason",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))
                Box {
                    Text(
                        text = reason,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isReasonMenuExpanded = true }
                            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    DropdownMenu(
                        expanded = isReasonMenuExpanded,
                        onDismissRequest = { isReasonMenuExpanded = false }
                    ) {
                        reasonOptions.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    reason = it
                                    isReasonMenuExpanded = false

                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Enter Price") },
                leadingIcon = { Text("Rs.") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable

fun AdminDashboardScreen(viewModel: AdminViewModel = viewModel(), onLogOut: () -> Unit) {

    var selectedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDate) {
        viewModel.loadOrders(selectedDate)
    }

    val isLoading = viewModel.isLoading.value
    val foodOrders = viewModel.foodOrders.value

    val totalPriceAll = foodOrders
        .flatMap { it.orders }
        .sumOf { it.quantity * it.price }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
        ) {
            ExpandableFoodListHeader(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                onLogOut = onLogOut,
                totalPriceAll = totalPriceAll
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF7F50))
                    }
                }

                foodOrders.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No orders found for this date.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(foodOrders) { foodOrder ->
                            ExpandableFoodCard(foodOrder, viewModel = viewModel)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        if (totalPriceAll != 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                color = Color(0xFFFF7F50),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Price",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Rs. $totalPriceAll",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

        }

    }
}


@Composable
fun ExpandableFoodCard(foodOrder: FoodOrder, viewModel: AdminViewModel) {
    var expanded by remember { mutableStateOf(false) }

    val totalQuantity = foodOrder.orders.sumOf { it.quantity }
    val totalPrice = foodOrder.orders.sumOf { it.quantity * it.price }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {


            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {

                    Text(
                        foodOrder.foodName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Qty:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("$totalQuantity", fontSize = 14.sp, color = Color.Black)


                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            "Total:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Rs. $totalPrice", fontSize = 14.sp, color = Color.Black)

                    }


                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                foodOrder.orders.forEach { order ->
                    EmployeeOrderItem(order)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EmployeeOrderItem(order: EmployeeOrder) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
            .padding(5.dp)
    ) {
        Text("${order.employeeName}", fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text("Quantity: ", color = Color.Black, fontWeight = FontWeight.Bold)
                Text("${order.quantity}", color = Color.Black)

            }

            Row {
                Text("Price: ", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(2.dp))
                Text("Rs. ${order.price}", color = Color.Black)

            }
        }

        if (!order.note.isNullOrBlank()) {
            Row {
                Text("Note: ", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(2.dp))

                Text("${order.note}", color = Color.Black)
            }

        }
    }
}


@Composable
fun ExpandableFoodListHeader(
    totalPriceAll: Int,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onLogOut: () -> Unit,
    showSummary: Boolean = false
) {
    //   val dateDialogState = rememberMaterialDialogState()
    var showDatePicker by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.End

    ) {

        IconButton(onClick = { onLogOut() }, modifier = Modifier.padding(end = 8.dp)) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = "Logout",
                tint = Color(0xFFFF7F50)
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Orders for ${formatDateKMP(selectedDate)}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Surface(
            modifier = Modifier
                .clickable { showDatePicker = true }
                .height(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFF7F50),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Filter by Date",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filter",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                if (showDatePicker) {
                    DatePickerDemo(
                        onDismiss = { showDatePicker = false } // Allow dismissing
                    )
                }
            }
        }
    }
    Text(
        text = "Order Information",
        modifier = Modifier.padding(10.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Spacer(modifier = Modifier.height(12.dp))


}



