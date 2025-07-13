package com.example.investidorapp.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.investidorapp.MainActivity
import com.example.investidorapp.R
import android.util.Log

class NotificationService(private val context: Context) {
    
    companion object {
        const val PRICE_ALERT_CHANNEL_ID = "price_alerts"
        const val GENERAL_CHANNEL_ID = "general_notifications"
    }

    init {
        Log.d("NotificationService", "Inicializando NotificationService...")
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        Log.d("NotificationService", "Criando canais de notificação...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Canal para alertas de preços
            val priceAlertChannel = NotificationChannel(
                PRICE_ALERT_CHANNEL_ID,
                "Alertas de Preços",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações sobre mudanças de preços de ações"
                enableVibration(true)
                enableLights(true)
            }

            // Canal para notificações gerais
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "Notificações Gerais",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações gerais do app"
            }

            notificationManager.createNotificationChannel(priceAlertChannel)
            notificationManager.createNotificationChannel(generalChannel)
            Log.d("NotificationService", "Canais criados!")
        }
    }

    fun showPriceAlertNotification(title: String, message: String, symbol: String) {
        Log.d("NotificationService", "Tentando mostrar notificação: $title - $message")
        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        val intent = Intent(context, com.example.investidorapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("symbol", symbol)
            putExtra("notification_type", "price_alert")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, PRICE_ALERT_CHANNEL_ID)
            .setSmallIcon(com.example.investidorapp.R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Padrão de vibração
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("NotificationService", "Permissão concedida, mostrando notificação!")
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } else {
            Log.e("NotificationService", "Permissão de notificação NÃO concedida!")
        }
    }

    fun showGeneralNotification(title: String, message: String) {
        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    fun showPortfolioUpdateNotification(portfolioValue: Double, change: Double, changePercent: Double) {
        val title = "📊 Atualização do Portfólio"
        val message = "Valor: R$ ${String.format("%.2f", portfolioValue)} " +
                     "(${if (change >= 0) "+" else ""}${String.format("%.2f", changePercent)}%)"
        
        showGeneralNotification(title, message)
    }

    fun showMarketNewsNotification(newsTitle: String) {
        val title = "📰 Notícia do Mercado"
        val message = newsTitle
        
        showGeneralNotification(title, message)
    }
} 