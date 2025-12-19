package com.example.smartshop.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartshop.data.local.dao.CartDao
import com.example.smartshop.data.local.dao.OrderDao
import com.example.smartshop.data.local.dao.ProductDao
import com.example.smartshop.data.local.entity.CartItem
import com.example.smartshop.data.local.entity.Order
import com.example.smartshop.data.local.entity.Product

@Database(
    entities = [
        Product::class,
        CartItem::class,
        Order::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartshop_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}