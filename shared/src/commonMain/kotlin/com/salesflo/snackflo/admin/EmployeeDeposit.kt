package com.salesflo.snackflo.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesflo.snackflo.repository.UserViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesflo.snackflo.repository.Deposit
import com.salesflo.snackflo.repository.GroupedOrder
import com.salesflo.snackflo.repository.Order
import com.salesflo.snackflo.repository.updateInitialAmount
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Composable
fun EmpListScreen(viewModel: UserViewModel = viewModel { UserViewModel() }) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    val searchQuery = remember { mutableStateOf("") }

    val users = viewModel.employeeUsers
    val filteredUsers = users.filter {
        it.username?.contains(searchQuery.value, ignoreCase = true) == true
    }

    Column(modifier = Modifier.fillMaxSize()) {

        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            placeholder = { Text("Search employee") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
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

        LazyColumn(
            contentPadding = PaddingValues(horizontal =  16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredUsers) { user ->
                val userId = user.userId.toString()
                val isExpanded = expandedStates[userId] ?: false

                LaunchedEffect(isExpanded) {
                    if (isExpanded && !viewModel.userOrders.containsKey(userId)) {
                        println("USERID $userId")
                        viewModel.fetchUserDetails(userId)
                    }
                }

                EmployeeExpandableCard(
                    employeeName = user.username ?: "Unknown",
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedStates[userId] = !isExpanded },
                    orders = viewModel.userOrders[userId],
                    deposit = viewModel.userDeposit[userId],
                    isLoading = viewModel.isLoading[userId] ?: false,
                    userId = userId,
                    viewModel = viewModel
                )
            }
        }
    }
}



@Composable
fun EmployeeExpandableCard(
    employeeName: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    orders: List<Order>?,
    deposit: Deposit?,
    isLoading: Boolean,
    userId: String,
    viewModel: UserViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputAmount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isExpanded) 0.dp else 16.dp,
                bottomEnd = if (isExpanded) 0.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = employeeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Amount",
                        tint = Color(0xFFFF7F50)
                    )
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
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                tonalElevation = 12.dp,
                title = {
                    Text(
                        "Add Deposit Amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A202C)
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Enter the amount to add to ${employeeName}'s account",
                            fontSize = 14.sp,
                            color = Color(0xFF718096),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = inputAmount,
                            singleLine = true,
                            onValueChange = { inputAmount = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Amount (Rs.)", color = Color(0xFF718096)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF7F50),
                                focusedLabelColor = Color(0xFFFF7F50)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = inputAmount.toIntOrNull()
                            if (amount != null) {
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        updateInitialAmount(userId, amount, selectedDate)
                                        showDialog = false
                                        inputAmount = ""
                                        viewModel.fetchUserDetails(userId)
                                    } catch (e: Exception) {
                                        println("Firestore Error: ${e.message}")
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting && inputAmount.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSubmitting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFFF7F50)
                                )
                                Text("Adding...", color = Color(0xFFFF7F50))
                            }
                        } else {
                            Text("Add Amount", color = Color(0xFFFF7F50), fontWeight = FontWeight.Medium)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF718096))
                    }
                }
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFF7F50))
                                Text("Loading details...", color = Color(0xFF718096))
                            }
                        }
                    } else {
                        val totalSpent = orders?.sumOf { it.price?.toInt() ?: 0 } ?: 0
                        val initialAmount = deposit?.initialAmount?.toInt() ?: 0
                        val currentBalance = initialAmount - totalSpent

                        SummarySection(
                            title = "Employee Summary",
                            icon = Icons.Default.Person
                        ) {
                            SummaryRow("Total Deposit", "Rs. ${deposit?.initialAmount ?: "0"}")
                            SummaryRow("Current Balance", "Rs. $currentBalance", isHighlighted = true)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

//                        val groupedOrders = orders?.groupBy { it.date }?.mapValues { (_, ordersForDate) ->
//                            val totalPrice = ordersForDate.sumOf { order ->
//                                order.price?.toInt() ?: 0
//                            }
//                            GroupedOrder(
//                                date = ordersForDate.first().date,
//                                totalPrice = totalPrice,
//                                orderCount = ordersForDate.size
//                            )
//                        }?.values?.sortedByDescending { it.date }

                        SummaryRow("Total Spent", "Rs. $totalSpent", isHighlighted = false)

                        Spacer(modifier = Modifier.height(20.dp))

                        val groupedOrders = orders?.groupBy { it.date } ?: emptyMap()

                        val groupedDepositsMap = viewModel.userDeposits[userId]?.groupBy { it.date } ?: emptyMap()


                        val allDates = (groupedOrders.keys + groupedDepositsMap.keys).distinct().sortedDescending()

                        SummarySection(
                            title = "Transaction History",
                            icon = Icons.Default.Timeline
                        ) {
                            if (allDates.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color(0xFF718096),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Text(
                                                text = "No orders found",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF718096),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    allDates.forEach { date ->
                                        // Show orders for this date
                                        groupedOrders[date]?.let { ordersForDate ->
                                            val totalPrice = ordersForDate.sumOf { it.price ?: 0 }
                                            val groupedOrder = GroupedOrder(
                                                date = date,
                                                totalPrice = totalPrice,
                                                orderCount = ordersForDate.size
                                            )
                                            OrderDateCard(groupedOrder = groupedOrder)
                                        }


                                        groupedDepositsMap[date]?.let { depositsForDate ->
                                            val totalDeposit = depositsForDate.sumOf { it.initialAmount ?: 0 }
                                            val groupedDeposit = GroupedOrder(
                                                date = date,
                                                totalPrice = totalDeposit,
                                                orderCount = depositsForDate.size
                                            )
                                            DepositDateCard(groupedDeposit = groupedDeposit) // You'll define this below
                                        }
                                    }

//                                    if (allDates.size > 5) {
//                                        Card(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7)),
//                                            shape = RoundedCornerShape(12.dp)
//                                        ) {
//                                            Text(
//                                                text = "+ ${allDates.size - 5} more days with activity",
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = Color(0xFF4A5568),
//                                                fontWeight = FontWeight.Medium,
//                                                modifier = Modifier.padding(16.dp),
//                                                textAlign = TextAlign.Center
//                                            )
//                                        }
//                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



data class TimelineEntry(
    val date: String,
    val type: String, // "order" or "deposit"
    val orderData: GroupedOrder?,
    val depositData: DepositEntry?
)

data class DepositEntry(
    val date: String,
    val amount: Int,
    val type: String
)

@Composable
fun DepositDateCard(groupedDeposit: GroupedOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.AccountBalanceWallet,
//                        contentDescription = null,
//                        tint = Color(0xFF38B2AC),
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = groupedDeposit.date.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "Rs. ${groupedDeposit.totalPrice}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF008000)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${groupedDeposit.orderCount} deposits made",
                fontSize = 12.sp,
                color = Color(0xFF718096)
            )
        }
    }
}

@Composable
fun DepositDateCard1(depositEntry: DepositEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF10B981)) // Green border for deposits
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, // or Icons.Default.AccountBalance
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            Color(0xFF10B981).copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(4.dp)
                )
                Column {
                    Text(
                        text = depositEntry.date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3748)
                    )
                    Text(
                        text = "Deposit",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "+Rs. ${depositEntry.amount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFF7F50),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1A202C)
            )
        }
        content()
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFFFFF5F5) else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A5568),
                fontSize = 14.sp
            )
            Text(
                text = value,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlighted) Color(0xFFFF7F50) else Color(0xFF1A202C),
                fontSize = if (isHighlighted) 16.sp else 14.sp
            )
        }
    }
}

@Composable
private fun OrderDateCard(groupedOrder: GroupedOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = groupedOrder.date ?: "Unknown Date",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A202C)
                    )
                    Text(
                        text = "${groupedOrder.orderCount} order${if (groupedOrder.orderCount != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = Color(0xFF718096),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFF7F50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Rs. ${groupedOrder.totalPrice}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

data class GroupedOrder(
    val date: String,
    val totalPrice: Int,
    val orderCount: Int
)













