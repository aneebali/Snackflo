package com.salesflo.snackflo.common

import com.salesflo.snackflo.repository.Deposit
import com.salesflo.snackflo.repository.NewOrder
import kotlinx.datetime.LocalDate

fun formatDateKMP(date: LocalDate): String {
    val month = date.monthNumber.toString().padStart(2, '0')
    val day = date.dayOfMonth.toString().padStart(2, '0')
    return "${date.year}-$month-$day" // "yyyy-MM-dd"
}
fun generateRandomId(length: Int = 20): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

sealed class DatedTransactionItem(val date: String) {
    class OrderItem(val order: NewOrder) : DatedTransactionItem(order.date)
    class DepositItem(val deposit: Deposit) : DatedTransactionItem(deposit.date)
}
