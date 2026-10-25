Repositorio para empresa 'Cappucare', hecho en Android Studio
Integrantes: Benjamín Herrera   benja.herreram@duocuc.cl

Contexto de la empresa:
'Cappucare es una organización dedicada a la venta de productos e insumos médicos, orientada a disponer de artículos
necesarios para distintas necesidades de atención y cuidado. Debido a la variedad de productos que comercializa, requiere
mantener un control adecuado de sus existencias, disponibilidad y movimientos, procurando contar con información
actualizada para responder oportunamente a las solicitudes de sus clientes.'

Recursos nativos usados
| Cámara | Tomar foto del producto en el formulario | `CAMERA` (se pide en ejecución) |
| Notificaciones | Aviso de stock bajo | `POST_NOTIFICATIONS` (Android 13+) |

Arquitectura:

Patrón de uso MVVM
/
com.example.cappucare
    data/        # Room: entidades (Producto, Movimiento), DAO y base de datos
    logic/       # Validador (reglas de validación) y Notificador
    viewmodel/   # InventarioViewModel (estado de la UI)
    ui/          # Pantallas en Jetpack Compose
    MainActivity.kt   # Navegación (NavHost


Librerias: 
- Jetpack Compose + Material 3
- Navigation Compose
- ViewModel + StateFlow
- Room (con KSP)
