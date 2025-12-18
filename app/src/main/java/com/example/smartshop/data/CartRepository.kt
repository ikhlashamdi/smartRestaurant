package com.example.smartshop.data


import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

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
                val db = AppDatabase.getDatabase(context)
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

    // ===== PANIER =====

    fun getCartItems(userId: String): Flow<List<CartItem>> {
        return cartDao.getCartItems(userId)
    }

    suspend fun addToCart(product: Product, userId: String, quantity: Int = 1) {
        Log.d(TAG, "Ajout au panier: ${product.name} x$quantity")

        // Vérifier si le produit est déjà dans le panier
        val existingItem = cartDao.getCartItemByProduct(product.id, userId)

        if (existingItem != null) {
            // Mettre à jour la quantité
            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
            cartDao.updateCartItem(updatedItem)
        } else {
            // Ajouter un nouvel item
            val cartItem = CartItem(
                id = java.util.UUID.randomUUID().toString(),
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

    // ===== COMMANDES =====

    fun getOrders(userId: String): Flow<List<Order>> {
        return orderDao.getOrders(userId)
    }

    suspend fun createOrder(userId: String, cartItems: List<CartItem>): Order {
        Log.d(TAG, "Création commande pour ${cartItems.size} articles")

        // Convertir les items du panier en OrderItems
        val orderItems = cartItems.map {
            OrderItem(it.productName, it.quantity, it.productPrice)
        }

        // Calculer le total
        val total = cartItems.sumOf { it.getTotalPrice() }

        // Créer la commande
        val order = Order(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            items = Gson().toJson(orderItems),
            totalAmount = total,
            orderDate = System.currentTimeMillis(),
            status = "En cours"
        )

        // Sauvegarder la commande
        orderDao.insertOrder(order)

        // Mettre à jour les stocks des produits
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

        // Vider le panier
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