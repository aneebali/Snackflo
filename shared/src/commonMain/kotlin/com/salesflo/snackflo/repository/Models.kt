package com.salesflo.snackflo.repository
import com.salesflo.snackflo.common.DatedTransactionItem
import kotlinx.serialization.Serializable

data class UserOrderSummary(
    val userId: String,
    val username: String,
    val totalPrice: Int,
    val totalQuantity: Int
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
data class UnitOptions(
    val options: List<String> = emptyList()
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
    val orderId : String = "",
    val categoryName: String? = ""
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


data class DateOrderSummary(
    val date: String,
    val totalAmount: Int,
    val orderCount: Int
)

@Serializable
data class Deposit(
    val userId: String = "",
    val amount: Int = 0,
    val initialAmount: Int = 0,
    val date: String = ""
)
@Serializable
data class FirestoreUser(
    val userType: String? = null,
    val userId: String? = null,
    val username: String? = null,
)

data class Order(
    val date: String = "",
    val price: Int = 0
)
data class GroupedOrder(
    val date: String?,
    val totalPrice: Int,
    val orderCount: Int
)

data class GroupedTransaction(
    val date: String,
    val orders: List<DatedTransactionItem.OrderItem>,
    val deposits: List<DatedTransactionItem.DepositItem>,
    val totalSpent: Int
)

@Serializable
data class NewOrder(
    val userId: String = "",
    val date: String = "",
    val price: Int = 0,
    val orderDetails: String = ""
)

