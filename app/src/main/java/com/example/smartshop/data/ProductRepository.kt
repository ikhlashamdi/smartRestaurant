package com.example.smartshop.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ProductRepository private constructor(
    private val productDao: ProductDao,
    private val context: Context
) {

    companion object {
        @Volatile
        private var INSTANCE: ProductRepository? = null

        fun getInstance(context: Context): ProductRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ProductRepository(
                    AppDatabase.getDatabase(context).productDao(),
                    context
                )
                INSTANCE = instance
                instance
            }
        }
    }

    fun getProducts(userId: String? = null): Flow<List<Product>> {
        return if (userId != null) {
            productDao.getProductsByUser(userId)
        } else {
            productDao.getAllProducts()
        }
    }

    suspend fun addProduct(product: Product) {
        if (product.price <= 0 || product.quantity < 0) {
            throw IllegalArgumentException("Données invalides")
        }
        val newProduct = product.copy(
            id = if (product.id.isEmpty()) java.util.UUID.randomUUID().toString() else product.id
        )
        productDao.insertProduct(newProduct)
        FirestoreService.saveProduct(newProduct, newProduct.userId)
    }

    suspend fun updateProduct(product: Product) {
        if (product.price <= 0 || product.quantity < 0) {
            throw IllegalArgumentException("Données invalides")
        }
        productDao.updateProduct(product)
        FirestoreService.updateProduct(product, product.userId)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
        FirestoreService.deleteProduct(product.id)
    }

    // ✅ Méthode pour synchroniser depuis Firestore vers Room
    suspend fun syncProductFromFirestore(product: Product) {
        val existingProduct = productDao.getProductById(product.id)

        if (existingProduct == null) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
        }
    }

    suspend fun getProductCount(userId: String? = null): Int {
        return if (userId != null) {
            productDao.getProductCountByUser(userId)
        } else {
            productDao.getProductCount()
        }
    }

    suspend fun getTotalStockValue(userId: String? = null): Double {
        return if (userId != null) {
            productDao.getTotalStockValueByUser(userId) ?: 0.0
        } else {
            productDao.getTotalStockValue()
        }
    }
}