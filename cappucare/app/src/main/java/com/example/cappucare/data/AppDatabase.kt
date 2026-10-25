package com.example.cappucare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Producto::class, Movimiento::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): InventarioDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun get(ctx: Context): AppDatabase =
            instancia ?: synchronized(this) {
                Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "cappucare.db")
                    .build().also { instancia = it }
            }
    }
}