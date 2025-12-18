package com.example.smartshop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Product::class,
        CartItem::class,
        Order::class
    ],
    version = 3, // ✅ Increment version number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao // ✅ Add CartDao
    abstract fun orderDao(): OrderDao // ✅ Add OrderDao

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
                    .fallbackToDestructiveMigration() // ✅ Recreate DB on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}