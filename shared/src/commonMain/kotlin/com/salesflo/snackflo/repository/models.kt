package com.salesflo.snackflo.repository

import kotlinx.serialization.Serializable


@Serializable
data class Items(
    val id: String = "",
    val name: String = "",
    val Image: String = "",
    val Price: Int = 0
)

data class FoodOrder(
    val foodName: String,
    val orders: List<EmployeeOrder>
)

data class EmployeeOrder(
    val employeeName: String,
    val quantity: Int,
    val documentId: String,
    val price: Int,
    val status: String?,
    val rejectReason: String?,
    val note: String?
)

data class SelectedOrderItem(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val price: Int = 0,
    val quantity: Int = 0,
    val note: String = "",
    val date: String = "",
    val rejectReason: String = "",
    val itemName: String = "",
    val empId: String = "",
    val status: String = "Pending"
)

data class FoodSummary(
    val foodName: String,
    val note: String,
    val totalQuantity: Int
)

data class AuthResult(
    val success: Boolean,
    val userType: String? = null,
    val userId: String? = null,
    val username: String? = null
)