package com.example.cappucare.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cappucare.data.Producto
import com.example.cappucare.viewmodel.InventarioViewModel
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun CampoTexto(
    valor: String, onCambio: (String) -> Unit, etiqueta: String,
    icono: ImageVector, error: String?, teclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor, onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) Icon(Icons.Default.Error, "error", tint = MaterialTheme.colorScheme.error)
        },
        isError = error != null,
        supportingText = { AnimatedVisibility(visible = error != null) { Text(error ?: "") } }, // animacion del error
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(vm: InventarioViewModel, onNuevo: () -> Unit, onMov: (Int) -> Unit) {
    val lista by vm.productos.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cappucare - Inventario") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) { Icon(Icons.Default.Add, "Agregar producto") }
        }
    ) { padding ->
        if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay productos todavía")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lista, key = { it.id }) { p -> ProductoCard(p) { onMov(p.id) } }
            }
        }
    }
}

@Composable
fun ProductoCard(p: Producto, onClick: () -> Unit) {
    val bajo = p.cantidad <= p.minimo
    // animacion: cambia de color si el stock esta bajo
    val color by animateColorAsState(
        if (bajo) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "colorCard"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { onClick() }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (bajo) Icons.Default.Warning else Icons.Default.Inventory2, null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(p.nombre, style = MaterialTheme.typography.titleMedium)
                Text("${p.codigo} · ${p.categoria} · ${p.ubicacion}")
                Text("Stock: ${p.cantidad} ${p.unidad} (mín. ${p.minimo})")
                if (bajo) Text("¡Reponer!", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(vm: InventarioViewModel, onVolver: () -> Unit) {
    val ctx = LocalContext.current
    val f = vm.form
    val e = vm.errores

    // recurso nativo 1: camara
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bmp?.let { vm.guardarFoto(it) }
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) camara.launch(null)
        else Toast.makeText(ctx, "Sin permiso de cámara no se puede tomar la foto", Toast.LENGTH_SHORT).show()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Nuevo producto") },
            navigationIcon = { IconButton(onClick = { vm.limpiarForm(); onVolver() }) { Icon(Icons.Default.ArrowBack, "Volver") } }
        )
    }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CampoTexto(f.codigo, { vm.cambiarForm(f.copy(codigo = it)) }, "Código", Icons.Default.QrCode, e.codigo)
            CampoTexto(f.nombre, { vm.cambiarForm(f.copy(nombre = it)) }, "Nombre", Icons.Default.Label, e.nombre)
            CampoTexto(f.categoria, { vm.cambiarForm(f.copy(categoria = it)) }, "Categoría", Icons.Default.Category, e.categoria)
            CampoTexto(f.cantidad, { vm.cambiarForm(f.copy(cantidad = it)) }, "Cantidad", Icons.Default.Numbers, e.cantidad, KeyboardType.Number)
            CampoTexto(f.unidad, { vm.cambiarForm(f.copy(unidad = it)) }, "Unidad de medida", Icons.Default.Straighten, e.unidad)
            CampoTexto(f.minimo, { vm.cambiarForm(f.copy(minimo = it)) }, "Stock mínimo", Icons.Default.Warning, e.minimo, KeyboardType.Number)
            CampoTexto(f.ubicacion, { vm.cambiarForm(f.copy(ubicacion = it)) }, "Ubicación", Icons.Default.Place, e.ubicacion)

            OutlinedButton(
                onClick = {
                    val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (ok) camara.launch(null) else permisoCamara.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, null)
                Spacer(Modifier.width(8.dp))
                Text("Tomar foto del producto")
            }

            // preview de la foto
            f.fotoPath?.let { ruta ->
                val img = remember(ruta) { BitmapFactory.decodeFile(ruta)?.asImageBitmap() }
                img?.let {
                    Image(it, "Foto del producto", Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
                }
            }

            Button(onClick = { vm.guardarProducto(onVolver) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(vm: InventarioViewModel, id: Int, onVolver: () -> Unit) {
    val ctx = LocalContext.current
    val p = vm.seleccionado
    val e = vm.erroresMov
    val historial by remember(id) { vm.historial(id) }.collectAsState(initial = emptyList())

    var tipo by remember { mutableStateOf("SALIDA") }
    var cantidad by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("Venta") }

    // pido permiso de notificaciones al entrar (Android 13+)
    val permisoNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(id) {
        vm.cargarProducto(id)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) permisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(p?.nombre ?: "Movimiento") },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") } }
        )
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Stock actual: ${p?.cantidad ?: 0} ${p?.unidad ?: ""}", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tipo == "INGRESO", onClick = { tipo = "INGRESO"; motivo = "Recepción de mercadería" }, label = { Text("Ingreso") })
                FilterChip(selected = tipo == "SALIDA", onClick = { tipo = "SALIDA"; motivo = "Venta" }, label = { Text("Salida / Venta") })
            }

            CampoTexto(cantidad, { cantidad = it }, "Cantidad", Icons.Default.Numbers, e.cantidad, KeyboardType.Number)
            CampoTexto(motivo, { motivo = it }, "Motivo", Icons.Default.Description, e.motivo)

            Button(
                onClick = { vm.registrarMovimiento(tipo, cantidad, motivo) { cantidad = "" } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Registrar movimiento") }

            Text("Historial", style = MaterialTheme.typography.titleSmall)
            val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
            LazyColumn {
                items(historial, key = { it.id }) { m ->
                    ListItem(
                        headlineContent = { Text("${m.tipo}: ${m.cantidad}") },
                        supportingContent = { Text("${m.motivo} · ${fmt.format(Date(m.fecha))}") },
                        leadingContent = {
                            Icon(if (m.tipo == "INGRESO") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, null)
                        }
                    )
                }
            }
        }
    }
}