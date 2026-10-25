package com.example.cappucare.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cappucare.data.AppDatabase
import com.example.cappucare.data.InventarioRepository
import com.example.cappucare.data.Movimiento
import com.example.cappucare.data.Producto
import com.example.cappucare.logic.ErroresMov
import com.example.cappucare.logic.ErroresProducto
import com.example.cappucare.logic.FormProducto
import com.example.cappucare.logic.Notificador
import com.example.cappucare.logic.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class InventarioViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = InventarioRepository(AppDatabase.get(app))

    val productos: StateFlow<List<Producto>> = repo.productos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var form by mutableStateOf(FormProducto())
        private set
    var errores by mutableStateOf(ErroresProducto())
        private set
    private var intentoGuardar = false

    var seleccionado by mutableStateOf<Producto?>(null)
        private set
    var erroresMov by mutableStateOf(ErroresMov())
        private set

    init {
        viewModelScope.launch {
            if (repo.contar() == 0) {
                repo.insertar(Producto(codigo = "GUA-001", nombre = "Guantes de nitrilo", categoria = "Protección", cantidad = 120, unidad = "caja", minimo = 20, ubicacion = "Bodega A"))
                repo.insertar(Producto(codigo = "MAS-002", nombre = "Mascarillas quirúrgicas", categoria = "Protección", cantidad = 8, unidad = "caja", minimo = 15, ubicacion = "Bodega A"))
                repo.insertar(Producto(codigo = "JER-003", nombre = "Jeringas 5ml", categoria = "Insumos clínicos", cantidad = 50, unidad = "unidad", minimo = 10, ubicacion = "Bodega B"))
            }
        }
    }


    fun cambiarForm(nuevo: FormProducto) {
        form = nuevo
        if (intentoGuardar) errores = Validador.producto(nuevo)
    }

    fun limpiarForm() {
        form = FormProducto()
        errores = ErroresProducto()
        intentoGuardar = false
    }

    fun guardarFoto(bmp: Bitmap) {
        val archivo = File(getApplication<Application>().filesDir, "prod_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        form = form.copy(fotoPath = archivo.absolutePath)
    }

    fun guardarProducto(onOk: () -> Unit) {
        intentoGuardar = true
        val e: ErroresProducto = Validador.producto(form)
        errores = e
        if (e.hayError()) return

        viewModelScope.launch {
            if (repo.existeCodigo(form.codigo.trim())) {
                errores = e.copy(codigo = "Ya existe un producto con ese código")
                return@launch
            }
            repo.insertar(
                Producto(
                    codigo = form.codigo.trim(),
                    nombre = form.nombre.trim(),
                    categoria = form.categoria.trim(),
                    cantidad = form.cantidad.toInt(),
                    unidad = form.unidad.trim(),
                    minimo = form.minimo.toInt(),
                    ubicacion = form.ubicacion.trim(),
                    fotoPath = form.fotoPath
                )
            )
            limpiarForm()
            onOk()
        }
    }


    fun cargarProducto(id: Int) {
        erroresMov = ErroresMov()
        viewModelScope.launch { seleccionado = repo.porId(id) }
    }

    fun historial(id: Int): Flow<List<Movimiento>> = repo.movimientos(id)

    fun registrarMovimiento(tipo: String, cantidad: String, motivo: String, onOk: () -> Unit) {
        val p: Producto = seleccionado ?: return
        val e: ErroresMov = Validador.movimiento(tipo, cantidad, motivo, p.cantidad)
        erroresMov = e
        if (e.hayError()) return

        val c: Int = cantidad.toInt()
        val nuevoStock: Int = if (tipo == "INGRESO") p.cantidad + c else p.cantidad - c

        viewModelScope.launch {
            val actualizado = p.copy(cantidad = nuevoStock)
            val mov = Movimiento(
                productoId = p.id,
                tipo = tipo,
                cantidad = c,
                motivo = motivo.trim(),
                fecha = System.currentTimeMillis()
            )
            repo.registrarMovimiento(actualizado, mov)
            seleccionado = actualizado

            if (nuevoStock <= p.minimo) {
                Notificador.avisar(getApplication(), p.nombre, nuevoStock)
            }
            onOk()
        }
    }
}