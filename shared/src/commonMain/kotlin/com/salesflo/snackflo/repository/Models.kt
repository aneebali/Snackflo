package com.salesflo.snackflo.repository
import kotlinx.serialization.Serializable

data class UserOrderSummary(
    val userId: String,
    val username: String,
    val totalPrice: Int,
    val totalQuantity: Int
)

data class FoodOrder(
    val foodName: String,
    val orders: List<EmployeeOrder>
)
@Serializable
data class EmployeeOrder(
    val employeeName: String,
    val quantity: Int,
    val documentId: String,
    val price : Int,
    val note : String?
)


@Serializable
data class SelectedOrderItems(
    val Itemid: String = "",
    val price: Int = 0,
    val itemName : String = "",
    val image: String = "",
    val quantity: String= "",
    val note: String = "",
    val date: String = "" ,
    val time : String = "",
    val empId : String = "",
    val categoryId : Int = 0,
    val orderId : String = ""
)

@Serializable
data class AuthResult(
    val success: Boolean,
    val userType: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val isDeviceInfo : Int = 1
)

@Serializable
data class Restaurant(
    val id: Int = -1,
    val name: String = ""
)

@Serializable
data class Item(
    val id: String = "",
    val Image: String = "",
    val categoryId: Int = -1,
    val name: String = "",
    val Price: Int = 0,
    val unit : String = ""
)

data class RestaurantWithItems(
    val restaurant: Restaurant,
    val items: List<Item>
)

data class RestaurantUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EmployeeOrders(
    val empId: String,
    val empName: String,
    val items: List<SelectedOrderItems>
)

data class RestaurantOrderData(
    val restaurant: Restaurant,
    val employeeOrders: List<EmployeeOrders>
)
