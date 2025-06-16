package com.salesflo.snackflo.employee

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.salesflo.snackflo.DatePickerDemo
import com.salesflo.snackflo.common.formatDateKMP
import com.salesflo.snackflo.repository.getOrdersForUserByDate
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.salesflo.snackflo.repository.SelectedOrderItems
import kotlinx.datetime.LocalDate


@Composable
fun EmployeeFoodListScreen(
    userId: String,
    onLogOut: () -> Unit,
    onFabClick: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<SelectedOrderItems>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    // var selectedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val totalPrice = orders.sumOf { it.price }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
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


    LaunchedEffect(selectedDate) {
        isLoading = true
        getOrdersForUserByDate(
            userId = userId,
            date = selectedDate,
            onResult = {
                orders = it
                isLoading = false
            },
            onError = {
                isLoading = false
            }
        )
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
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))


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
                        text = "Employee",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )


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
    )

    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ){
            Column(   modifier = Modifier
                .fillMaxSize()
            ) {
                Header(
                    onDateSelected = { selectedDate = it },
                    onLogOut = {
                        scope.launch { drawerState.open() }
                    },)



                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable { }
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF7F50),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp,),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Total Rs. $totalPrice",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFFF7F50))
                } else if (orders.isEmpty()) {
                    Text("No orders placed for this date.",   modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 5.dp,
                            bottom = 5.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        items(orders) { order ->
                            EmployeeOrderHistoryCard(
                                title = order.itemName,
                                description = order.note ?: "",
                                quantity = order.quantity.toString(),
                                price = order.price,
                                date = order.date,
                                imageUrl = order.image

                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { onFabClick() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 16.dp),

                containerColor = Color(0xFFFF7F50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Order", tint = Color.White)
            }
            DatePickerDemo(
                showDialog = showDatePicker,
                onDismiss = { showDatePicker = false },
                onDateSelected = { date ->
                    selectedDate = date
                    //  onDateSelected(selectedDate)

                })
        }
    }
}


@Composable
fun EmployeeOrderHistoryCard(
    imageUrl: String,
    title: String,
    description: String,
    quantity: String,
    //  status: String,
    price: Int,
    date: String,
    // reason: String

) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Qty: $quantity", fontSize = 14.sp)
                Text("Price: Rs. $price", fontSize = 14.sp)
                //   Text("Total: Rs. ${price.toInt() * quantity.toInt()}", fontSize = 14.sp)
                if (description.isNotBlank()) {
                    Text("Note: $description", fontSize = 14.sp)
                }

            }
        }
    }
}


@Composable
fun Header(
    onDateSelected: (LocalDate) -> Unit,
    onLogOut: () -> Unit,
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
                    text ="Dashboard",
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




