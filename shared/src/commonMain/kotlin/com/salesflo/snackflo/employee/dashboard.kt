package com.salesflo.snackflo.employee


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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.salesflo.snackflo.repository.SelectedOrderItem
import com.salesflo.snackflo.repository.formatDateKMP
import com.salesflo.snackflo.repository.getOrdersForUserByDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun EmployeeFoodListScreen(
    userId: String,
    onLogOut: () -> Unit,
    onFabClick: () -> Unit,
) {

    var orders by remember { mutableStateOf<List<SelectedOrderItem>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }

    val selectedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date


    // val dateDialogState = rememberMaterialDialogState()


    val totalPrice = orders.sumOf { it.quantity * it.price }

    // Load orders when date changes
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

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onLogOut) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFFF7F50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        .clickable { }
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .clickable { }
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFF7F50),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (orders.isEmpty()) {
                Text("No orders placed for this date.")
            } else {
                LazyColumn {

                    items(orders) { order ->
                        EmployeeOrderHistoryCard(
                            imageUrl = order.image,
                            title = order.itemName,
                            description = order.note ?: "",
                            quantity = order.quantity,
                            status = order.status,
                            price = order.price,
                            date = order.date,
                            reason = order.rejectReason
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onFabClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFFFF7F50)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Order", tint = Color.White)
        }

//        // Calendar dialog
//        MaterialDialog(
//            dialogState = dateDialogState,
//            shape = RoundedCornerShape(16.dp),
//            backgroundColor = Color.White,
//            buttons = {
//                positiveButton("OK", textStyle = TextStyle(color = Color(0xFFFF7F50)))
//                negativeButton("Cancel", textStyle = TextStyle(color = Color.Gray))
//            }
//        ) {
//            datepicker(
//                initialDate = selectedDate,
//                colors = DatePickerDefaults.colors(
//                    headerBackgroundColor = Color(0xFFFF7F50),
//                    dateActiveBackgroundColor = Color(0xFFFF7F50),
//                    dateActiveTextColor = Color.White,
//                    dateInactiveTextColor = Color.Gray,
//                    calendarHeaderTextColor = Color.Black,
//                )
//            ) { date ->
//                selectedDate = date
//            }
//        }
    }
}


@Composable
fun EmployeeOrderHistoryCard(
    imageUrl: String,
    title: String,
    description: String,
    quantity: Int,
    status: String,
    price: Int,
    date: String,
    reason: String

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
                Text("Total: Rs. ${price.toInt() * quantity.toInt()}", fontSize = 14.sp)
                if (description.isNotBlank()) {
                    Text("Note: $description", fontSize = 14.sp)
                }
//
//                }


                // Text("Date: $date", fontSize = 14.sp)
                //  Text("Status: $status", fontSize = 14.sp)
//                if(reason.isNotEmpty() and reason.isNotBlank()){
//                    Text("Reason: $reason", fontSize = 14.sp)
//                }
//                if (reason.isNotBlank()) {
//                    Text(reason, fontSize = 12.sp, color = Color.Gray)
//                }
            }
        }
    }
}




