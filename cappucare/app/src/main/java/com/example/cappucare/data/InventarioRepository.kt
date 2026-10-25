package com.example.cappucare.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class InventarioRepository(private val db: AppDatabase) {
    private val dao: InventarioDao = db.dao()
    val productos: Flow<List<Producto>> = dao.productos()
    suspend fun porId(id: Int): Producto? = dao.productoPorId(id)
    suspend fun existeCodigo(codigo: String): Boolean = dao.existeCodigo(codigo) > 0
    suspend fun contar(): Int = dao.contar()
    suspend fun insertar(p: Producto) = dao.insertarProducto(p)
    fun movimientos(id: Int): Flow<List<Movimiento>> = dao.movimientos(id)

    suspend fun registrarMovimiento(producto: Producto, mov: Movimiento) {
        db.withTransaction {
            dao.actualizarProducto(producto)
            dao.insertarMov(mov)
        }
    }
}