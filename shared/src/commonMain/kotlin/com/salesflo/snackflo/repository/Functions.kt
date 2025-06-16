package com.salesflo.snackflo.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesflo.snackflo.common.AppConstant
import com.salesflo.snackflo.common.formatDateKMP
import com.salesflo.snackflo.common.generateRandomId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.*


class EmployeeViewModel : ViewModel() {
    fun submitSelectedOrder(
        selectedOrders: List<SelectedOrderItems>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val db = Firebase.firestore
            val batch = db.batch()

            selectedOrders.forEach { order ->
                val orderId = generateRandomId()
                val orderData = mapOf(
                    AppConstant.ORDER_ID to orderId,
                    AppConstant.USER_ID to order.empId,
                    AppConstant.ITEM_ID to order.Itemid,
                    AppConstant.PRICE to 0,
                    AppConstant.QUANTITY to order.quantity,
                    "note" to order.note,
                    AppConstant.DATE to order.date,
                    AppConstant.TIME to order.time,
                    AppConstant.CATEGORY_ID to order.categoryId
                )
                val docRef = db.collection(AppConstant.NEW_ORDERS).document(orderId)
                batch.set(docRef, orderData)
            }

            try {
                batch.commit()
                onSuccess()
            } catch (e: Exception) {
                println("Batch commit failed: $e")
                onFailure()
            }
        }
    }
}



suspend fun getTodayOrderSummaryPerUser(
    date: LocalDate,
    onResult: (List<UserOrderSummary>) -> Unit,
    onError: (Throwable) -> Unit
) {
    val db = Firebase.firestore
    val formattedDate = formatDateKMP(date)

    try {
        val snapshot = db.collection(AppConstant.NEW_ORDERS)
            .where(AppConstant.DATE, equalTo = formattedDate)
            .get()

        if (snapshot.documents.isEmpty()) {
            onResult(emptyList())
            return
        }

        val orders = snapshot.documents.mapNotNull { doc ->
            val userId = doc.get<String>(AppConstant.USER_ID)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val price = doc.get<Int>(AppConstant.PRICE) ?: return@mapNotNull null
            userId to price
        }

        val userSummaries = orders
            .groupBy { it.first }
            .map { (userId, userOrders) ->
                val totalPrice = userOrders.sumOf { it.second }
                userId to totalPrice
            }

        val userIds = userSummaries.map { it.first }.toSet()
        val usersMap = fetchUsersBatch(db, userIds)

        val result = userSummaries.map { (userId, totalPrice) ->
            val userData = usersMap[userId]
            val username = userData?.get<String>(AppConstant.USERNAME)?.takeIf { it.isNotBlank() } ?: "Unknown User"

            UserOrderSummary(
                userId = userId,
                username = username,
                totalPrice = totalPrice,
                totalQuantity = 0
            )
        }.sortedByDescending { it.totalPrice }

        println("ORDER SUMMARY RESULT: ${result.size} users found")
        onResult(result)

    } catch (e: Exception) {
        println("Error fetching order summaries: ${e.message}")
        onError(e)
    }
}

private suspend fun fetchUsersBatch(
    db: FirebaseFirestore,
    userIds: Set<String>
): Map<String, DocumentSnapshot?> {
    if (userIds.isEmpty()) return emptyMap()

    return coroutineScope {
        userIds.map { userId ->
            async {
                try {
                    userId to db.collection(AppConstant.USERS).document(userId).get()
                } catch (e: Exception) {
                    println("Error fetching user $userId: ${e.message}")
                    userId to null
                }
            }
        }.awaitAll().toMap()
    }
}


fun getOrdersForUserByDate(
    userId: String,
    date: LocalDate,
    onResult: (List<SelectedOrderItems>) -> Unit,
    onError: (Throwable) -> Unit
) {
    val db = Firebase.firestore
    val formattedDate = formatDateKMP(date)
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val snapshot = db
                .collection(AppConstant.NEW_ORDERS)
                .where(AppConstant.USER_ID, equalTo = userId)
                .where(AppConstant.DATE, equalTo = formattedDate)
                .get()

            val orders = mutableListOf<SelectedOrderItems>()

            for (doc in snapshot.documents) {
                val baseOrder = doc.data<SelectedOrderItems>()

                val itemId = doc.get<String>(AppConstant.ITEM_ID) ?: ""
                var itemName = ""
                var itemPrice = 0
                var itemImage = ""

                if (itemId.isNotEmpty()) {
                    val itemDoc = db.collection(AppConstant.ITEMS_LIST)
                        .document(itemId)
                        .get()
                    itemName = itemDoc.get<String>(AppConstant.ITEM_NAME) ?: ""
                    itemImage = itemDoc.get<String>(AppConstant.ITEM_IMAGE) ?: ""
                }

                val updatedOrder = baseOrder.copy(
                    itemName = itemName,
                    image = itemImage
                )

                orders.add(updatedOrder)
            }

            println(orders)
            onResult(orders)
        } catch (e: Exception) {
            println("error")
            println(e)
            onError(e)
        }
    }
}




fun groupOrdersByRestaurantAndEmployee(
    orderItems: List<SelectedOrderItems>,
    restaurants: List<Restaurant>,
    items: List<Item>,
    employeeNames: Map<String, String>
): List<RestaurantOrderData> {

    val restaurantMap = restaurants.associateBy { it.id }

    val groupedOrders = orderItems.groupBy { it.categoryId }
        .mapNotNull { (categoryId, orders) ->
            val restaurant = restaurantMap[categoryId]
            if (restaurant != null) {
                val employeeOrders = orders.groupBy { it.empId }
                    .map { (empId, empOrders) ->
                        EmployeeOrders(
                            empId = empId,
                            empName = employeeNames[empId] ?: "Employee $empId",
                            items = empOrders
                        )
                    }
                RestaurantOrderData(
                    restaurant = restaurant,
                    employeeOrders = employeeOrders
                )
            } else null
        }

    return groupedOrders
}

class OrderRepository {
    private val firestore = Firebase.firestore

    // Orders flow with date filter
    fun getOrdersFlow(
        selectedDate: String,
        itemMap: Map<String, String>,
        employeeNames: Map<String, String>
    ): Flow<List<SelectedOrderItems>> {
        return firestore.collection(AppConstant.NEW_ORDERS)
            .where(AppConstant.DATE, equalTo  = selectedDate)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        val itemId = document.get<String>(AppConstant.ITEM_ID) ?: ""
                        SelectedOrderItems(
                            Itemid = document.get<String>(AppConstant.ITEM_ID) ?: "",
                            price = document.get<Int>(AppConstant.PRICE) ?: 0,
                            itemName = itemMap[itemId] ?: "Unknown Item",
                            orderId = document.get<String>(AppConstant.ORDER_ID),
                            quantity = document.get<String>(AppConstant.QUANTITY) ?: "",
                            date = document.get<String>(AppConstant.DATE) ?: "",
                            time = document.get<String>(AppConstant.TIME) ?: "",
                            empId = document.get<String>(AppConstant.USER_ID) ?: "",
                            categoryId = document.get<Int>(AppConstant.CATEGORY_ID) ?: 0
                        )
                    } catch (e: Exception) {
                        println("Error parsing document: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                println("Error in orders flow: ${e.message}")
                emit(emptyList())
            }
    }

    // Employee names flow
    fun getEmployeeNamesFlow(): Flow<Map<String, String>> {
        return firestore.collection(AppConstant.USERS)
            .snapshots
            .map { snapshot ->
                snapshot.documents.associate { document ->
                    val empId = document.id
                    val empName = document.get<String>(AppConstant.USERNAME) ?: "Unknown Employee"
                    empId to empName
                }
            }
            .catch { e ->
                println("Error in employee names flow: ${e.message}")
                emit(emptyMap())
            }
    }

    // Restaurants flow
    fun getRestaurantsFlow(): Flow<List<Restaurant>> {
        return firestore.collection(AppConstant.RESTAURANT)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        Restaurant(
                            id = document.get<Int>(AppConstant.RESTAURANT_ID) ?: -1,
                            name = document.get<String>(AppConstant.RESTAURANT_NAME) ?: ""
                        )
                    } catch (e: Exception) {
                        println("Error parsing restaurant: ${e.message}")
                        null
                    }
                }.ifEmpty {
                    // Fallback restaurants
                    listOf(
                        Restaurant(id = 1, name = "Restaurant 1"),
                        Restaurant(id = 2, name = "Restaurant 2"),
                        Restaurant(id = 3, name = "Restaurant 3")
                    )
                }
            }
            .catch { e ->
                println("Error in restaurants flow: ${e.message}")
                emit(listOf(
                    Restaurant(id = 1, name = "Restaurant 1"),
                    Restaurant(id = 2, name = "Restaurant 2"),
                    Restaurant(id = 3, name = "Restaurant 3")
                ))
            }
    }

    fun getItemsFlow(): Flow<List<Item>> {
        return firestore.collection(AppConstant.ITEMS_LIST)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    try {
                        Item(
                            id = document.id,
                            name = document.get<String>(AppConstant.ITEM_NAME) ?: ""
                        )
                    } catch (e: Exception) {
                        println("Error parsing item: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                println("Error in items flow: ${e.message}")
                emit(emptyList())
            }
    }

    suspend fun loadOrdersFromFirestore(
        itemMap: Map<String, String>,
        employeeNames: Map<String, String>,
        selectedDate: String
    ): List<SelectedOrderItems> {
        return try {
            val snapshot = firestore.collection(AppConstant.NEW_ORDERS)
                .where("date" , equalTo = selectedDate).get()
            snapshot.documents.mapNotNull { document ->
                try {
                    val itemId = document.get<String>(AppConstant.ITEM_ID) ?: ""
                    SelectedOrderItems(
                        Itemid = document.get<String>(AppConstant.ITEM_ID) ?: "",
                        price = document.get<Int>(AppConstant.PRICE) ?: 0,
                        itemName = itemMap[itemId] ?: "Unknown Item",
                        orderId = document.get<String>(AppConstant.ORDER_ID),
                        quantity = document.get<String>(AppConstant.QUANTITY) ?: "",
                        date = document.get<String>(AppConstant.DATE) ?: "",
                        time = document.get<String>(AppConstant.TIME) ?: "",
                        empId = document.get<String>(AppConstant.USER_ID) ?: "",
                        categoryId = document.get<Int>(AppConstant.CATEGORY_ID) ?: 0
                    )
                } catch (e: Exception) {
                    println("Error parsing document: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error loading orders: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadEmployeeNamesFromFirestore(): Map<String, String> {
        return try {
            val snapshot = firestore.collection(AppConstant.USERS).get()
            snapshot.documents.associate { document ->
                val empId = document.id
                val empName = document.get<String>(AppConstant.USERNAME) ?: "Unknown Employee"
                empId to empName
            }
        } catch (e: Exception) {
            println("Error loading employee names: ${e.message}")
            emptyMap()
        }
    }

    suspend fun loadRestaurantsFromFirestore(): List<Restaurant> {
        return try {
            val snapshot = firestore.collection(AppConstant.RESTAURANT).get()
            snapshot.documents.mapNotNull { document ->
                try {
                    Restaurant(
                        id = document.get<Int>(AppConstant.RESTAURANT_ID) ?: -1,
                        name = document.get<String>(AppConstant.RESTAURANT_NAME) ?: ""
                    )
                } catch (e: Exception) {
                    println("Error parsing restaurant: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error loading restaurants: ${e.message}")
            listOf(
                Restaurant(id = 1, name = "Restaurant 1"),
                Restaurant(id = 2, name = "Restaurant 2"),
                Restaurant(id = 3, name = "Restaurant 3")
            )
        }
    }

    suspend fun loadItemsFromFirestore(): List<Item> {
        return try {
            val snapshot = firestore.collection(AppConstant.ITEMS_LIST).get()
            snapshot.documents.mapNotNull { document ->
                try {
                    Item(
                        id = document.id,
                        name = document.get<String>(AppConstant.ITEM_NAME) ?: ""
                    )
                } catch (e: Exception) {
                    println("Error parsing item: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error loading items: ${e.message}")
            emptyList()
        }
    }
}

@OptIn(ExperimentalTime::class)
class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _orderItems = MutableStateFlow<List<SelectedOrderItems>>(emptyList())
    val orderItems: StateFlow<List<SelectedOrderItems>> = _orderItems.asStateFlow()

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _employeeNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val employeeNames: StateFlow<Map<String, String>> = _employeeNames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow("")

    fun setupRealtimeListeners(selectedDate: LocalDate) {
        val selectedDateFormatted = formatDateKMP(selectedDate)
        _selectedDate.value = selectedDateFormatted

        viewModelScope.launch {

            repository.getRestaurantsFlow()
                .collect { restaurants ->
                    _restaurants.value = restaurants
                }
        }

        viewModelScope.launch {

            repository.getItemsFlow()
                .collect { items ->
                    _items.value = items
                }
        }

        viewModelScope.launch {

            repository.getEmployeeNamesFlow()
                .collect { employeeNames ->
                    _employeeNames.value = employeeNames
                }
        }


        viewModelScope.launch {
            combine(
                repository.getItemsFlow(),
                repository.getEmployeeNamesFlow(),
                _selectedDate
            ) { items, employeeNames, date ->
                Triple(
                    items.associateBy({ it.id }, { it.name }),
                    employeeNames,
                    date
                )
            }.flatMapLatest { (itemMap, employeeNames, date) ->
                if (date.isNotEmpty()) {
                    repository.getOrdersFlow(date, itemMap, employeeNames)
                } else {
                    flowOf(emptyList())
                }
            }.collect { orders ->
                _orderItems.value = orders
            }
        }
    }
    fun loadAllData(selectedDate: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val restaurantsDeferred = async { repository.loadRestaurantsFromFirestore() }
                val itemsDeferred = async { repository.loadItemsFromFirestore() }
                val employeeNamesDeferred = async { repository.loadEmployeeNamesFromFirestore() }

                val restaurants = restaurantsDeferred.await()
                val items = itemsDeferred.await()
                val employeeNames = employeeNamesDeferred.await()

                val itemMap = items.associateBy({ it.id }, { it.name })
                val selectedDateFormatted = formatDateKMP(selectedDate)

                val orders = repository.loadOrdersFromFirestore(itemMap, employeeNames, selectedDateFormatted)

                _orderItems.value = orders
                _restaurants.value = restaurants
                _items.value = items
                _employeeNames.value = employeeNames

            } catch (e: Exception) {
                println("Error loading data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
suspend fun submitPrices(prices: Map<String, Int>, orderData: List<RestaurantOrderData>) {
    val db = Firebase.firestore
    println(orderData.size)

    orderData.forEach { restaurant ->
        restaurant.employeeOrders.forEach { emp ->
            emp.items.forEach { item ->
                println(item.orderId)
                val key = "${restaurant.restaurant.id}_${emp.empId}_${item.Itemid}"
                prices[item.orderId]?.let { price ->

                    println("🔧 Updating ${item.orderId} with price $price")

                    db.collection(AppConstant.NEW_ORDERS)
                        .document(item.orderId)
                        .update(AppConstant.PRICE to price)
                }
            }
        }
    }
}



