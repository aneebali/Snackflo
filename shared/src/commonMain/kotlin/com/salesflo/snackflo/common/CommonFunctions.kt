package com.salesflo.snackflo.common

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