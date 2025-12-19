package com.example.smartshop.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartshop.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>


    @Query("SELECT * FROM products WHERE userId = :userId")
    fun getProductsByUser(userId: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int


    @Query("SELECT COUNT(*) FROM products WHERE userId = :userId")
    suspend fun getProductCountByUser(userId: String): Int

    @Query("SELECT SUM(price * quantity) FROM products")
    suspend fun getTotalStockValue(): Double


    @Query("SELECT SUM(price * quantity) FROM products WHERE userId = :userId")
    suspend fun getTotalStockValueByUser(userId: String): Double?


    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): Product?
}