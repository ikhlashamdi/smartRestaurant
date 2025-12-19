package com.example.smartshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 0,
    val userId: String = ""
) {
    constructor() : this("", "", "", 0.0, 0, "")

    fun getTotalPrice(): Double = productPrice * quantity
}