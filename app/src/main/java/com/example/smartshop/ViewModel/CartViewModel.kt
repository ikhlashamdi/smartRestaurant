package com.example.smartshop.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.data.local.entity.CartItem
import com.example.smartshop.data.repository.CartRepository
import com.example.smartshop.data.local.entity.Order
import com.example.smartshop.data.local.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "CartViewModel"
    private val repository = CartRepository.getInstance(application)

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _orderCreated = MutableStateFlow(false)
    val orderCreated: StateFlow<Boolean> = _orderCreated

    fun setUser(userId: String) {
        Log.d(TAG, "setUser: $userId")
        _currentUserId.value = userId

        // Observer le panier
        viewModelScope.launch {
            repository.getCartItems(userId).collect { items ->
                _cartItems.value = items
                updateCartTotal()
            }
        }


        viewModelScope.launch {
            repository.getOrders(userId).collect { orders ->
                _orders.value = orders
            }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val userId = _currentUserId.value ?: return
        Log.d(TAG, "Ajout au panier: ${product.name} x$quantity")

        viewModelScope.launch {
            repository.addToCart(product, userId, quantity)
        }
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(item, newQuantity)
            updateCartTotal()
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(item)
            updateCartTotal()
        }
    }

    fun clearCart() {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.clearCart(userId)
            _cartTotal.value = 0.0
        }
    }

    fun createOrder() {
        val userId = _currentUserId.value ?: return
        val items = _cartItems.value

        if (items.isEmpty()) {
            Log.w(TAG, "Panier vide, impossible de créer une commande")
            return
        }

        Log.d(TAG, "Création d'une commande avec ${items.size} articles")

        viewModelScope.launch {
            try {
                repository.createOrder(userId, items)
                _orderCreated.value = true
                Log.d(TAG, "Commande créée avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la création de la commande", e)
            }
        }
    }

    fun resetOrderCreated() {
        _orderCreated.value = false
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(order, newStatus)
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    private suspend fun updateCartTotal() {
        val userId = _currentUserId.value ?: return
        val total = repository.getCartTotal(userId)
        _cartTotal.value = total
        Log.d(TAG, "Total panier: $total€")
    }
}