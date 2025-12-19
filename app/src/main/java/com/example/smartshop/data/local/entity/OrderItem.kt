package com.example.smartshop.data.local.entity

data class OrderItem(
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
) {
    constructor() : this("", 0, 0.0)
}