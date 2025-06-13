package com.salesflo.snackflo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform