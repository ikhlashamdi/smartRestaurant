package com.example.smartshop.data

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
    val imageUrl: String = "" // ✅ Nouveau champ pour l'URL de l'image
) {
    constructor() : this("", "", 0, 0.0, "", "")
}