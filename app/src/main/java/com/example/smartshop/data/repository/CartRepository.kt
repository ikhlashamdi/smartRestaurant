package com.example.smartshop.data.repository

import android.content.Context
import android.util.Log
import com.example.smartshop.data.local.dao.CartDao
import com.example.smartshop.data.local.dao.OrderDao
import com.example.smartshop.data.local.dao.ProductDao
import com.example.smartshop.data.local.database.AppDatabase
import com.example.smartshop.data.local.entity.CartItem
import com.example.smartshop.data.local.entity.Order
import com.example.smartshop.data.local.entity.OrderItem
import com.example.smartshop.data.local.entity.Product
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CartRepository private constructor(
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val productDao: ProductDao
) {

    companion object {
        private const val TAG = "CartRepository"

        @Volatile
        private var INSTANCE: CartRepository? = null

        fun getInstance(context: Context): CartRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.Companion.getDatabase(context)
                val instance = CartRepository(
                    db.cartDao(),
                    db.orderDao(),
                    db.productDao()
                )
                INSTANCE = instance
                instance
            }
        }
    }



    fun getCartItems(userId: String): Flow<List<CartItem>> {
        return cartDao.getCartItems(userId)
    }

    suspend fun addToCart(product: Product, userId: String, quantity: Int = 1) {
        Log.d(TAG, "Ajout au panier: ${product.name} x$quantity")


        val existingItem = cartDao.getCartItemByProduct(product.id, userId)

        if (existingItem != null) {

            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
            cartDao.updateCartItem(updatedItem)
        } else {

            val cartItem = CartItem(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                quantity = quantity,
                userId = userId
            )
            cartDao.insertCartItem(cartItem)
        }
    }

    suspend fun updateCartItemQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(item)
        } else {
            cartDao.updateCartItem(item.copy(quantity = newQuantity))
        }
    }

    suspend fun removeFromCart(item: CartItem) {
        cartDao.deleteCartItem(item)
    }

    suspend fun clearCart(userId: String) {
        cartDao.clearCart(userId)
    }

    suspend fun getCartTotal(userId: String): Double {
        return cartDao.getCartTotal(userId) ?: 0.0
    }



    fun getOrders(userId: String): Flow<List<Order>> {
        return orderDao.getOrders(userId)
    }

    suspend fun createOrder(userId: String, cartItems: List<CartItem>): Order {
        Log.d(TAG, "Création commande pour ${cartItems.size} articles")


        val orderItems = cartItems.map {
            OrderItem(it.productName, it.quantity, it.productPrice)
        }


        val total = cartItems.sumOf { it.getTotalPrice() }


        val order = Order(
            id = UUID.randomUUID().toString(),
            userId = userId,
            items = Gson().toJson(orderItems),
            totalAmount = total,
            orderDate = System.currentTimeMillis(),
            status = "En cours"
        )


        orderDao.insertOrder(order)


        cartItems.forEach { cartItem ->
            val product = productDao.getProductById(cartItem.productId)
            if (product != null) {
                val newQuantity = product.quantity - cartItem.quantity
                if (newQuantity >= 0) {
                    val updatedProduct = product.copy(quantity = newQuantity)
                    productDao.updateProduct(updatedProduct)
                    Log.d(TAG, "Stock mis à jour: ${product.name} -> $newQuantity")
                }
            }
        }


        clearCart(userId)

        Log.d(TAG, "Commande créée avec succès: ${order.id}")
        return order
    }

    suspend fun updateOrderStatus(order: Order, newStatus: String) {
        val updatedOrder = order.copy(status = newStatus)
        orderDao.updateOrder(updatedOrder)
    }

    suspend fun deleteOrder(order: Order) {
        orderDao.deleteOrder(order)
    }

    suspend fun getOrderCount(userId: String): Int {
        return orderDao.getOrderCount(userId)
    }
}