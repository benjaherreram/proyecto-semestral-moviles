package com.example.cappucare.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo:         String,
    val nombre:         String,
    val categoria:      String,
    val cantidad:       Int,
    val unidad:         String,
    val minimo:         Int,
    val ubicacion:      String,
    val fotoPath:       String? = null
)

@Entity
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId:     Int,
    val tipo:           String,
    val cantidad:       Int,
    val motivo:         String,
    val fecha:          Long
)

@Dao
interface InventarioDao {
    @Query("SELECT * FROM Producto ORDER BY nombre")
    fun productos(): Flow<List<Producto>>

    @Query("SELECT * FROM Producto WHERE id = :id")
    suspend fun productoPorId(id: Int): Producto?

    @Query("SELECT COUNT(*) FROM Producto WHERE codigo = :codigo")
    suspend fun existeCodigo(codigo: String): Int

    @Query("SELECT COUNT(*) FROM Producto")
    suspend fun contar(): Int

    @Insert suspend fun insertarProducto(p: Producto)
    @Update suspend fun actualizarProducto(p: Producto)
    @Insert suspend fun insertarMov(m: Movimiento)

    @Query("SELECT * FROM Movimiento WHERE productoId = :id ORDER BY fecha DESC")
    fun movimientos(id: Int): Flow<List<Movimiento>>
}