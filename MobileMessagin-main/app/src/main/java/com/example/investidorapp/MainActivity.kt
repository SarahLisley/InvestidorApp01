package com.example.investidorapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.investidorapp.service.AutoMonitoringService
import com.example.investidorapp.ui.screens.HomeScreen
import com.example.investidorapp.ui.theme.InvestidorAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    
    // Create a dedicated coroutine scope for background operations
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize performance monitoring
        initializePerformanceMonitoring()
        
        // Verificar se foi aberto por notificação
        val symbol = intent.getStringExtra("symbol")
        val notificationType = intent.getStringExtra("notification_type")
        
        // Iniciar serviço de monitoramento automático
        startAutoMonitoringService()
        
        setContent {
            InvestidorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
        
        // Log para debug
        if (symbol != null || notificationType != null) {
            println("App aberto por notificação - Símbolo: $symbol, Tipo: $notificationType")
        }
        requestNotificationPermission()
        // Disparar notificação de teste
        val notificationService = com.example.investidorapp.service.NotificationService(this)
        notificationService.showPriceAlertNotification(
            "Teste de Notificação",
            "Se você vê isso, as notificações estão funcionando.",
            "TESTE"
        )
    }
    
    private fun initializePerformanceMonitoring() {
        // Monitor for potential ANR issues
        backgroundScope.launch {
            // Add performance monitoring logic here if needed
        }
    }
    
    private fun startAutoMonitoringService() {
        val serviceIntent = Intent(this, AutoMonitoringService::class.java)
        startService(serviceIntent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
    }
} 