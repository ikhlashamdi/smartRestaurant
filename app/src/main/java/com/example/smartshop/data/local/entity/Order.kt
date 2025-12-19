package com.example.smartshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val items: String = "",
    val totalAmount: Double = 0.0,
    val orderDate: Long = 0L,
    val status: String = "En cours"
) {
    constructor() : this("", "", "", 0.0, 0L, "En cours")
}