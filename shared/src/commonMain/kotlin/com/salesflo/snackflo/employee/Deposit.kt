package com.salesflo.snackflo.employee
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesflo.snackflo.common.DatedTransactionItem
import com.salesflo.snackflo.repository.DateOrderSummary
import com.salesflo.snackflo.repository.Deposit
import com.salesflo.snackflo.repository.GroupedTransaction
import com.salesflo.snackflo.repository.NewOrder
import com.salesflo.snackflo.repository.loadUserData

@Composable
fun DepositManagementScreen(
    userId: String,
    onBack: () -> Unit = {}
) {
    var userDeposit by remember { mutableStateOf<Deposit?>(null) }
    var transactionItems by remember { mutableStateOf<List<DatedTransactionItem>>(emptyList()) }
    var dateOrderSummaries by remember { mutableStateOf<List<DateOrderSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        loadUserData(userId) { deposit, items, error ->
            userDeposit = deposit
            transactionItems = items
            errorMessage = error
            isLoading = false
        }
    }

    val groupedItems = transactionItems
        .groupBy { it.date }
        .map { (date, items) ->
            val orderItems = items.filterIsInstance<DatedTransactionItem.OrderItem>()
            val depositItems = items.filterIsInstance<DatedTransactionItem.DepositItem>()
            val totalSpent = orderItems.sumOf { it.order.price }

            GroupedTransaction(
                date = date,
                orders = orderItems,
                deposits = depositItems,
                totalSpent = totalSpent
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF7F50))
            }
        } else if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF7F50).copy(alpha = 0.1f))
            ) {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFFF7F50),
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            userDeposit?.let { deposit ->
                val totalSpent = transactionItems.filterIsInstance<DatedTransactionItem.OrderItem>().sumOf { it.order.price }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Current Balance",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF718096)
                                )
                                Text(
                                    text = "Rs. ${deposit.initialAmount - totalSpent}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF7F50)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Total Deposit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF718096)
                                )
                                Text(
                                    text = "Rs. ${deposit.initialAmount}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF008000)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(
                                text =  "Total Spent: ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF718096)
                            )
                            Text(
                                text =  "Rs. $totalSpent",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Transaction History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (groupedItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "No history found for this user",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF718096),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(groupedItems) { groupedItem ->
                            Column {

                                if (groupedItem.orders.isNotEmpty()) {
                                    OrderCardNew(
                                        order = groupedItem.orders.first().order,
                                        date = groupedItem.date,
                                        totalAmount = groupedItem.totalSpent
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                                if (groupedItem.deposits.isNotEmpty()) {
                                    groupedItem.deposits.forEach { depositItem ->
                                        DepositCardNew(
                                            deposit = depositItem.deposit,
                                            date = groupedItem.date
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                    }
                                }

//                                if (groupedItem.deposits.isNotEmpty()) {
//                                    val totalDepositAmount = groupedItem.deposits.sumOf { it.deposit.initialAmount }
//                                    DepositCardNew(
//                                        deposit = groupedItem.deposits.first().deposit.copy(
//                                            initialAmount = totalDepositAmount
//                                        ),
//                                        date = groupedItem.date
//                                    )
//                                    Spacer(modifier = Modifier.height(5.dp))
//                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun OrderCardNew(order: NewOrder, date: String,totalAmount : Int ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFFF7F50).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color(0xFFFF7F50),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
//                    Text(
//                        text = "${summary.orderCount} order${if (summary.orderCount > 1) "s" else ""}",
//                        fontSize = 12.sp,
//                        color = Color(0xFF718096)
//                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if(totalAmount > 0) "Rs. -${totalAmount}" else "Rs. ${totalAmount}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    text = "Spent",
                    fontSize = 10.sp,
                    color = Color(0xFF718096),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DepositCardNew(deposit: Deposit,  date: String ) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFFF7F50).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color(0xFFFF7F50),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
//                    Text(
//                        text = "${summary.orderCount} order${if (summary.orderCount > 1) "s" else ""}",
//                        fontSize = 12.sp,
//                        color = Color(0xFF718096)
//                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (deposit.initialAmount > 0) "Rs. +${deposit.initialAmount}" else "Rs. ${deposit.initialAmount}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF008000)
                )
                Text(
                    text = "Deposit",
                    fontSize = 10.sp,
                    color = Color(0xFF718096),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
