package com.example.investidorapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class AutoMonitoringService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var priceAlertService: PriceAlertService
    
    companion object {
        private const val TAG = "AutoMonitoringService"
        private const val MONITORING_INTERVAL = 5 * 60 * 1000L // 5 minutos
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Serviço de monitoramento criado")
        
        // Inicializar serviços após o contexto estar disponível
        priceAlertService = PriceAlertService(
            FirebaseDatabaseService(),
            NotificationService(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Serviço de monitoramento iniciado")
        
        serviceScope.launch {
            startMonitoring()
        }
        
        return START_STICKY
    }

    private suspend fun startMonitoring() {
        // Atraso inicial para permitir que a UI seja renderizada sem sobrecarga.
        Log.d(TAG, "Aguardando 3 segundos antes do primeiro ciclo de monitoramento...")
        delay(3000L)

        while (true) {
            try {
                Log.d(TAG, "Iniciando ciclo de monitoramento...")

                // Buscar preços de ações populares
                priceAlertService.fetchPopularStocks()

                // Aguardar próximo ciclo
                delay(MONITORING_INTERVAL)

            } catch (e: Exception) {
                Log.e(TAG, "Erro no monitoramento: ${e.message}")
                delay(60 * 1000L) // Aguardar 1 minuto em caso de erro
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Serviço de monitoramento destruído")
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
} 