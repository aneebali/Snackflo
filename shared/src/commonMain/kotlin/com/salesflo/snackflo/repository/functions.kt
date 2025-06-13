package com.salesflo.snackflo.repository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesflo.snackflo.AppConstant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


suspend fun fetchItems(): List<Items> {
    val db = Firebase.firestore
    return try {
        val snapshot = db.collection(AppConstant.ITEMS_LIST).get()
        snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
            val item = doc.data<Items>()
            item?.copy(id = doc.id)
        }
    } catch (e: Exception) {
        println("Fetch error: $e")
        emptyList()
    }
}


suspend fun fetchOrdersByDate(date: String): List<FoodOrder> {
    val db = Firebase.firestore

    return try {
        val snapshot = db.collection("orders")
            .where("date", "==", date)
            .get()

        val grouped = mutableMapOf<String, MutableList<EmployeeOrder>>()

        for (doc in snapshot.documents) {
            val foodName = doc.get<String>("itemName") ?: continue

            val order = EmployeeOrder(
                employeeName = doc.get("employeeName") ?: "Unknown",
                quantity = doc.get("quantity") ?: 0,
                documentId = doc.id,
                price = doc.get("price") ?: 0,
                status = doc.get("status") ?: "",
                rejectReason = doc.get("rejectedReason") ?: "",
                note = doc.get("note") ?: ""
            )

            val list = grouped.getOrPut(foodName) { mutableListOf() }
            list.add(order)
        }

        grouped.map { (foodName, orders) ->
            FoodOrder(foodName, orders)
        }

    } catch (e: Exception) {
        println("Fetch error: $e")
        emptyList()
    }
}


class EmployeeViewModel : ViewModel() {


    fun generateRandomId(length: Int = 20): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun submitSelectedOrders(
        selectedOrders: List<SelectedOrderItem>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val db = Firebase.firestore
            val batch = db.batch()

            selectedOrders.forEach { order ->
                val orderId = generateRandomId()
                val orderData = mapOf(
                    "orderId" to orderId,
                    "userId" to order.empId,
                    "itemId" to order.id,
                    "itemName" to order.name,
                    "image" to order.image,
                    "price" to order.price,
                    "quantity" to order.quantity,
                    "note" to order.note,
                    "status" to order.status,
                    "date" to order.date,
                    "rejectReason" to order.rejectReason,
                    "employeeName" to order.itemName
                )
                val docRef = db.collection("orders").document(orderId)
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


fun formatDateKMP(date: LocalDate): String {
    val month = date.monthNumber.toString().padStart(2, '0')
    val day = date.dayOfMonth.toString().padStart(2, '0')
    return "${date.year}-$month-$day" // "yyyy-MM-dd"
}

fun getOrdersForUserByDate(
    userId: String,
    date: LocalDate,
    onResult: (List<SelectedOrderItem>) -> Unit,
    onError: (Throwable) -> Unit
) {
    val db = Firebase.firestore
    val formattedDate = formatDateKMP(date)

    CoroutineScope(Dispatchers.Default).launch {
        try {
            val snapshot = db
                .collection("orders")
                .where("userId", "==", userId)
                .where("date", "==", formattedDate)
                .get()

            val orders = snapshot.documents.map { doc ->
                doc.data<SelectedOrderItem>()
            }
            onResult(orders)
        } catch (e: Exception) {
            onError(e)
        }
    }
}

class AdminViewModel : ViewModel() {
    private val _foodOrders = mutableStateOf<List<FoodOrder>>(emptyList())
    val foodOrders: State<List<FoodOrder>> = _foodOrders

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loadOrders(date: LocalDate) {
        _isLoading.value = true
        val formattedDate = date.toString()

        // ✅ Launching suspend function correctly
        viewModelScope.launch {
            try {
                val orders = fetchOrdersByDate(formattedDate)
                _foodOrders.value = orders
            } catch (e: Exception) {
                _foodOrders.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// shared/src/commonMain

interface SettingsDataStore {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
}



