package com.example.smartshop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrders(userId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("SELECT COUNT(*) FROM orders WHERE userId = :userId")
    suspend fun getOrderCount(userId: String): Int
}