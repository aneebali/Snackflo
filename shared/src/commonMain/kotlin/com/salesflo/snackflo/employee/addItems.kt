package com.salesflo.snackflo.employee


import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.cmppreference.LocalPreference
import com.salesflo.snackflo.repository.EmployeeViewModel
import com.salesflo.snackflo.repository.Items
import com.salesflo.snackflo.repository.SelectedOrderItem
import com.salesflo.snackflo.repository.fetchItems
import com.salesflo.snackflo.showToast
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ItemScreenList(onArrowClick: () -> Unit, viewModel: EmployeeViewModel = viewModel()) {
    var itemList by remember { mutableStateOf<List<Items>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val selectedItems = remember { mutableStateMapOf<String, SelectedOrderItem>() }
    var orderSubmitted by remember { mutableStateOf(false) }


    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(Color.White),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Items") },
                navigationIcon = {
                    IconButton(onClick = { onArrowClick() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.White),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (selectedItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
//                        val hasInvalidDate = selectedItems.values.any {
//                            val selectedDate = LocalDate.parse(it.date)
//                            selectedDate.isBefore(LocalDate.now())
//                        }
//                        if (hasInvalidDate) {
//                            showToast("Orders cannot be placed for past dates")
//                            return@FloatingActionButton
//                        }


                        viewModel.submitSelectedOrders(
                            selectedItems.values.toList(),
                            onSuccess = {
                                showToast("Order placed successfully!")
                                orderSubmitted = true
                                selectedItems.values.clear()
                                onArrowClick()
                            },
                            onFailure = {
                                showToast("Failed to place order. Try again.")
                                orderSubmitted = true
                            }
                        )
                    },
                    containerColor = Color(0xFFFF7F50)
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = "Submit Order",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            try {
                val items = fetchItems()
                itemList = items
            } catch (e: Exception) {
                println("Error: $e")
            } finally {
                isLoading = false
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF7F50))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(itemList) { item ->
                    var qty by remember { mutableStateOf(0) }
                    var note by remember { mutableStateOf("") }
                    var selectedDate =
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val preference = LocalPreference.current

                    // val sharedPreferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                    val name = preference.getString("username") ?: ""
                    val id = preference.getString("userId") ?: ""

                    FoodOrderCard1(
                        item = item,
                        date = selectedDate.toString(),
                        quantity = qty,
                        onQuantityChange = {
                            qty = it
                            if (it > 0) {
                                selectedItems[item.id] = SelectedOrderItem(
                                    id = item.id,
                                    name = item.name,
                                    image = item.Image,
                                    price = item.Price,
                                    quantity = it,
                                    note = note,
                                    date = selectedDate.toString(),
                                    empId = id.toString(),
                                    itemName = name.toString(),
                                )
                            } else {
                                selectedItems.remove(item.id)
                            }
                        },
                        note = note,
                        onNoteChange = {
                            note = it
                            selectedItems[item.id]?.let { selected ->
                                selectedItems[item.id] = selected.copy(note = it)
                            }
                        },
                        selectedDate = selectedDate,
                        onDateSelected = {
                            selectedDate = it
                            selectedItems[item.id]?.let { selected ->
                                selectedItems[item.id] = selected.copy(date = it.toString())
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}


@Composable
fun FoodOrderCard1(
    item: Items,
    date: String,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {

    Card(
        modifier = Modifier
            .padding(10.dp)
            .background(color = Color.White)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                AsyncImage(
                    model = item.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Price: Rs.${item.Price}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF595959)
                        )
                        //      Text("Date: $selectedDate", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF595959))

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Qty:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF595959)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier.size(28.dp)
                                    .background(Color(0xFFFF7F50), CircleShape).clickable {
                                        onQuantityChange(quantity + 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Box(
                                modifier = Modifier.size(28.dp)
                                    .background(Color(0xFFFF7F50), CircleShape).clickable {
                                        if (quantity > 0) onQuantityChange(quantity - 1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Add Note") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFFFF7F50)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedLabelColor = Color(0xFFE91E63)
                    )
                )
            }
        }
    }
}







