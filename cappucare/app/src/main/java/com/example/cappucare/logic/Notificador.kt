package com.example.cappucare.logic

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notificador {
    private const val CANAL = "stock_bajo"

    fun avisar(ctx: Context, producto: String, stock: Int) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= 26) {
            val canal = NotificationChannel(CANAL, "Stock bajo", NotificationManager.IMPORTANCE_DEFAULT)
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        val n = NotificationCompat.Builder(ctx, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Stock bajo: $producto")
            .setContentText("Quedan $stock unidades, hay que reponer")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(producto.hashCode(), n)
    }
}