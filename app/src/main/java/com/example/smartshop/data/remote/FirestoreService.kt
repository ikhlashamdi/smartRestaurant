package com.example.smartshop.data.remote

import com.example.smartshop.data.local.entity.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val productsRef = db.collection("products")

    suspend fun saveProduct(product: Product, userId: String) {
        val productWithUser = product.copy(
            id = product.id.ifEmpty { productsRef.document().id },
            userId = userId
        )
        productsRef.document(productWithUser.id).set(productWithUser).await()
    }

    suspend fun updateProduct(product: Product, userId: String) {
        productsRef.document(product.id).set(product).await()
    }

    suspend fun deleteProduct(productId: String) {
        productsRef.document(productId).delete().await()
    }

    fun listenToProducts(
        userId: String,
        onSnapshot: (List<Product>) -> Unit
    ) {
        productsRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSnapshot(emptyList())
                    return@addSnapshotListener
                }
                val products = snapshot?.toObjects(Product::class.java) ?: emptyList()
                onSnapshot(products)
            }
    }
}