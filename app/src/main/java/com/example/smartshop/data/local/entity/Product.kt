package com.example.smartshop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val userId: String = "",
    val imageUrl: String = ""
) {
    constructor() : this("", "", 0, 0.0, "", "")
}