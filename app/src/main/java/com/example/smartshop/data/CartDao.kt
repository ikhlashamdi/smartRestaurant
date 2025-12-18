package com.example.smartshop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: String): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId AND userId = :userId LIMIT 1")
    suspend fun getCartItemByProduct(productId: String, userId: String): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Update
    suspend fun updateCartItem(item: CartItem)

    @Delete
    suspend fun deleteCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)

    @Query("SELECT SUM(productPrice * quantity) FROM cart_items WHERE userId = :userId")
    suspend fun getCartTotal(userId: String): Double?
}