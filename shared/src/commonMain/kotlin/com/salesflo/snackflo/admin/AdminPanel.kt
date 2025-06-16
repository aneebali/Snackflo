package com.salesflo.snackflo.admin

import AppPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesflo.snackflo.DatePickerDemo
import com.salesflo.snackflo.repository.EmployeeOrder
import com.salesflo.snackflo.repository.OrderViewModel
import com.salesflo.snackflo.repository.UserOrderSummary
import com.salesflo.snackflo.common.formatDateKMP
import com.salesflo.snackflo.repository.getTodayOrderSummaryPerUser
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Composable
fun AdminDashboardScreen(onLogOut: () -> Unit,
                         orderViewModel: OrderViewModel = viewModel{OrderViewModel()},
                         ) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentScreen by remember { mutableStateOf("Admin") }

    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }

    var selectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    var orders by remember { mutableStateOf<List<UserOrderSummary>>(emptyList()) }
    var totalPriceAll : Int = 0


    if(currentScreen == "EmployeeLedger"){
        LaunchedEffect(selectedDate) {
            isLoading = true
            getTodayOrderSummaryPerUser(
                date = selectedDate,
                onResult = {
                    println(orders)
                    orders = it
                    isLoading = false
                },
                onError = {
                    isLoading = false
                }
            )
        }
        totalPriceAll = orders.sumOf { it.totalPrice }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(280.dp)
                    .background(
                        color = Color.White

//                        Brush.verticalGradient(
//                            colors = listOf(
//                                Color(0xFFFF7F50),
//                                Color(0xFFFF6B35)
//                            )
//                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal =  24.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Modern Avatar with gradient
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )

                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp),
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val name = AppPreferences.userName

                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "Admin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Dashboard",
                        fontSize = 16.sp,
                        color =Color.Black,
                        modifier = Modifier
                            .clickable {  scope.launch { drawerState.close() }
                                currentScreen = "Admin"
                              //  onDashboardClick()
                            }
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    Text(
                        text = "Employee Ledger",
                        fontSize = 16.sp,
                        color =Color.Black,
                        modifier = Modifier
                            .clickable {  scope.launch { drawerState.close() }
                               // onLedgerClick()
                                currentScreen = "EmployeeLedger"
                            }
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    // Modern logout button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                onLogOut()
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFF7F50)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign Out",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                   // .padding(bottom = if (totalPriceAll != 0) 100.dp else 0.dp)
            ) {
                ExpandableFoodListHeader(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    onLogOut = {
                        scope.launch { drawerState.open() }
                    },
                    totalPriceAll = totalPriceAll,
                    currentScreen = currentScreen
                )
                when {
                    currentScreen == "EmployeeLedger" && isLoading -> {
                        // Loading indicator
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF7F50),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading orders...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    currentScreen == "EmployeeLedger" && orders.isEmpty() -> {
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

                    currentScreen == "Admin" -> {
                        OrderManagementScreen(viewModel = orderViewModel, selectedDate)

                    }

                    currentScreen == "EmployeeLedger" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 140.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders) { orderSummary ->
                                UserSummaryCard(orderSummary)
                            }
                        }
                    }
                }
            }

            // Modern bottom total bar
            if (totalPriceAll != 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start =  16.dp, end = 16.dp, bottom = 60.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF7F50),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Rs. $totalPriceAll",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
//                        Icon(
//                            imageVector = Icons.Default.TrendingUp,
//                            contentDescription = null,
//                            tint = Color.White,
//                            modifier = Modifier.size(24.dp)
//                        )
                    }
                }
            }
        }
    }
}



@Composable
fun EmployeeOrderItem(order: EmployeeOrder) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7FAFC),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.employeeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2D3748)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFA07A).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Rs. ${(order.quantity) *  (order.price)}",
                        color = Color.Black,
                            //Color(0xFF38A169),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color(0xFF718096),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Quantity: ${order.quantity}",
                    color = Color(0xFF718096),
                    fontSize = 14.sp
                )
            }

            if (!order.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = null,
                        tint = Color(0xFF718096),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.note,
                        color = Color(0xFF718096),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableFoodListHeader(
    currentScreen : String ,
    totalPriceAll: Int,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onLogOut: () -> Unit,
    showSummary: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal =  20.dp, vertical = 10.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    onClick = { onLogOut() },
                    shape = RoundedCornerShape(12.dp),
                    color =  Color(0xFFFF7F50).copy(alpha = 0.1f),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFFFF7F50),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Text(
                    text = if (currentScreen == "Admin")"Dashboard" else  "Employee Ledger",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Orders",
                        fontSize = 16.sp,
                        color = Color(0xFF718096)
                    )
                    Text(
                        text = formatDateKMP(selectedDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2D3748)
                    )
                }

                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFF7F50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Filter by Date",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filter",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    DatePickerDemo(
        showDialog = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateSelected = { date ->
            selectedDate = date
            onDateSelected(selectedDate)
        }
    )
}
