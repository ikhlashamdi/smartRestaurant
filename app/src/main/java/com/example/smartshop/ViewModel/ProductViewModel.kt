package com.example.smartshop.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.data.Product
import com.example.smartshop.data.ProductRepository
import com.example.smartshop.data.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ProductViewModel"
    private val repository = ProductRepository.getInstance(application)

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _stats = MutableStateFlow(Pair(0, 0.0))
    val stats: StateFlow<Pair<Int, Double>> = _stats

    fun setUser(userId: String) {
        Log.d(TAG, "🔑 setUser appelé avec userId: $userId")
        _currentUserId.value = userId

        // ✅ 1. Observer les produits depuis Room (base locale)
        viewModelScope.launch {
            Log.d(TAG, "📊 Démarrage de l'observation Room pour userId: $userId")
            repository.getProducts(userId).collect { localProducts ->
                Log.d(TAG, "📦 Room: ${localProducts.size} produit(s) local")
                _products.value = localProducts
                updateStats()
            }
        }

        // ✅ 2. Écouter les changements Firestore en temps réel
        Log.d(TAG, "🔥 Démarrage de l'écoute Firestore pour userId: $userId")
        FirestoreService.listenToProducts(userId) { firestoreProducts ->
            Log.d(TAG, "🔥 Callback Firestore reçu: ${firestoreProducts.size} produit(s)")

            viewModelScope.launch {
                // Synchroniser les produits Firestore vers Room
                firestoreProducts.forEach { product ->
                    try {
                        Log.d(TAG, "🔄 Synchronisation: ${product.name} (${product.id})")
                        repository.syncProductFromFirestore(product)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erreur sync: ${product.name}", e)
                    }
                }
            }
        }
    }

    fun addProduct(product: Product) {
        val userId = _currentUserId.value ?: return
        Log.d(TAG, "➕ Ajout produit: ${product.name}")

        viewModelScope.launch {
            try {
                val productWithUser = product.copy(userId = userId)
                repository.addProduct(productWithUser)
                Log.d(TAG, "✅ Produit ajouté avec succès")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "❌ Erreur ajout produit: ${e.message}", e)
            }
        }
    }

    fun updateProduct(product: Product) {
        Log.d(TAG, "🔄 Mise à jour produit: ${product.name}")

        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                Log.d(TAG, "✅ Produit mis à jour avec succès")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "❌ Erreur mise à jour produit: ${e.message}", e)
            }
        }
    }

    fun deleteProduct(product: Product) {
        Log.d(TAG, "🗑️ Suppression produit: ${product.name}")

        viewModelScope.launch {
            repository.deleteProduct(product)
            Log.d(TAG, "✅ Produit supprimé avec succès")
        }
    }

    private suspend fun updateStats() {
        val userId = _currentUserId.value ?: return
        val count = repository.getProductCount(userId)
        val totalValue = repository.getTotalStockValue(userId)
        Log.d(TAG, "📊 Stats mises à jour: $count produits, ${totalValue}€")
        _stats.value = Pair(count, totalValue)
    }
}