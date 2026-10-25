package com.example.cappucare.logic

data class FormProducto(
    val codigo:     String = "",
    val nombre:     String = "",
    val categoria:  String = "",
    val cantidad:   String = "",
    val unidad:     String = "",
    val minimo:     String = "",
    val ubicacion:  String = "",
    val fotoPath:   String? = null
)

data class ErroresProducto(
    val codigo:     String? = null,
    val nombre:     String? = null,
    val categoria:  String? = null,
    val cantidad:   String? = null,
    val unidad:     String? = null,
    val minimo:     String? = null,
    val ubicacion:  String? = null
) {
    fun hayError() = listOf(codigo, nombre, categoria, cantidad,
        unidad, minimo, ubicacion).any { it != null }
}

data class ErroresMov(val cantidad: String? = null, val motivo: String? = null) {
    fun hayError() = cantidad != null || motivo != null
}

object Validador {
    fun producto(f: FormProducto): ErroresProducto {
        val cant = f.cantidad.toIntOrNull()
        val min = f.minimo.toIntOrNull()
        return ErroresProducto(
            codigo = when {
                f.codigo.isBlank() -> "El código es obligatorio"
                !Regex("^[A-Za-z0-9-]{3,15}$").matches(f.codigo.trim()) -> "3 a 15 caracteres, solo letras, números o guion"
                else -> null
            },
            nombre = when {
                f.nombre.isBlank() -> "El nombre es obligatorio"
                f.nombre.trim().length < 3 -> "Mínimo 3 caracteres"
                else -> null
            },
            categoria = if (f.categoria.isBlank()) "Ingresa una categoría" else null,
            cantidad = when {
                f.cantidad.isBlank() -> "Ingresa la cantidad"
                cant == null -> "Debe ser un número entero"
                cant < 0 -> "No puede ser negativa"
                else -> null
            },
            unidad = if (f.unidad.isBlank()) "Ingresa la unidad (ej: caja, unidad)" else null,
            minimo = when {
                f.minimo.isBlank() -> "Ingresa el stock mínimo"
                min == null -> "Debe ser un número entero"
                min < 0 -> "No puede ser negativo"
                else -> null
            },
            ubicacion = if (f.ubicacion.isBlank()) "Ingresa la ubicación" else null
        )
    }

    fun movimiento(tipo: String, cantidad: String, motivo: String, stockActual: Int): ErroresMov {
        val c = cantidad.toIntOrNull()
        return ErroresMov(
            cantidad = when {
                cantidad.isBlank() -> "Ingresa la cantidad"
                c == null -> "Debe ser un número entero"
                c <= 0 -> "Debe ser mayor a 0"
                tipo == "SALIDA" && c > stockActual -> "No hay stock suficiente (disponible: $stockActual)"
                else -> null
            },
            motivo = if (motivo.trim().length < 3) "Indica el motivo (ej: Venta)" else null
        )
    }
}
