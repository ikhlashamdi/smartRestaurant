package com.example.smartshop.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    // ✅ Récupérer les produits d'un utilisateur spécifique
    @Query("SELECT * FROM products WHERE userId = :userId")
    fun getProductsByUser(userId: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // ✅ Compter les produits d'un utilisateur
    @Query("SELECT COUNT(*) FROM products WHERE userId = :userId")
    suspend fun getProductCountByUser(userId: String): Int

    @Query("SELECT SUM(price * quantity) FROM products")
    suspend fun getTotalStockValue(): Double

    // ✅ Valeur totale du stock d'un utilisateur
    @Query("SELECT SUM(price * quantity) FROM products WHERE userId = :userId")
    suspend fun getTotalStockValueByUser(userId: String): Double?

    // ✅ Nouvelle méthode pour récupérer un produit par ID
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): Product?
}