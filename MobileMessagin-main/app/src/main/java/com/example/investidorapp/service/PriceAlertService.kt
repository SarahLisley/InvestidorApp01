package com.example.investidorapp.service

import android.util.Log
import com.example.investidorapp.model.AlertType
import com.example.investidorapp.model.PriceAlert
import com.example.investidorapp.model.StockPrice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class PriceAlertService(
    private val databaseService: FirebaseDatabaseService,
    private val notificationService: NotificationService
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var monitoringJob: Job? = null
    private val currentPrices = ConcurrentHashMap<String, StockPrice>()
    private val stockPriceApiService = StockPriceApiService()

    fun startMonitoring() {
        Log.d("PriceAlertService", "Iniciando monitoramento de alertas...")
        monitoringJob = scope.launch {
            try {
                // Monitorar alertas ativos
                databaseService.getActiveAlerts().collectLatest { alerts ->
                    Log.d("PriceAlertService", "Monitorando ${alerts.size} alertas ativos")
                    
                    // Verificar cada alerta
                    alerts.forEach { alert ->
                        checkAlert(alert)
                    }
                }
            } catch (e: Exception) {
                Log.e("PriceAlertService", "Erro no monitoramento: ${e.message}")
            }
        }
    }

    // Função pública para verificar alertas manualmente
    fun checkAllAlerts() {
        scope.launch {
            try {
                Log.d("PriceAlertService", "Verificação manual de alertas iniciada")
                val alerts = databaseService.getActiveAlertsSync()
                Log.d("PriceAlertService", "Verificando ${alerts.size} alertas ativos")
                
                alerts.forEach { alert ->
                    checkAlert(alert)
                }
            } catch (e: Exception) {
                Log.e("PriceAlertService", "Erro na verificação manual: ${e.message}")
            }
        }
    }

    private suspend fun checkAlert(alert: PriceAlert) {
        try {
            Log.d("PriceAlertService", "Verificando alerta: ${alert.symbol} - Preço alvo: R$ ${String.format("%.2f", alert.targetPrice)} - Tipo: ${alert.alertType}")
            
            // Buscar preço simulado do Firebase primeiro
            val firebasePrice = databaseService.getStockPrice(alert.symbol)
            val currentPrice = firebasePrice ?: stockPriceApiService.getStockPrice(alert.symbol)
            
            if (currentPrice != null) {
                currentPrices[alert.symbol] = currentPrice
                
                Log.d("PriceAlertService", "Preço atual de ${alert.symbol}: R$ ${String.format("%.2f", currentPrice.price)}")
                
                // Salvar no Firebase para histórico
                databaseService.updateStockPrice(currentPrice)
                
                // Verificar se o alerta deve ser disparado
                val shouldTrigger = when (alert.alertType) {
                    AlertType.ABOVE -> currentPrice.price >= alert.targetPrice
                    AlertType.BELOW -> currentPrice.price <= alert.targetPrice
                }
                
                Log.d("PriceAlertService", "Deve disparar alerta? $shouldTrigger (Preço atual: ${currentPrice.price}, Alvo: ${alert.targetPrice})")
                
                if (shouldTrigger && alert.active) {
                    Log.d("PriceAlertService", "🚨 DISPARANDO ALERTA para ${alert.symbol}!")
                    // Disparar notificação
                    triggerAlert(alert, currentPrice)
                    
                    // Desativar alerta após disparar
                    databaseService.deactivateAlert(alert.id)
                }
            } else {
                Log.w("PriceAlertService", "Não foi possível obter preço para ${alert.symbol}")
            }
        } catch (e: Exception) {
            Log.e("PriceAlertService", "Erro ao verificar alerta ${alert.id}: ${e.message}")
        }
    }

    private suspend fun triggerAlert(alert: PriceAlert, currentPrice: StockPrice) {
        val title = when (alert.alertType) {
            AlertType.ABOVE -> "🚀 Alerta de Alta - ${alert.symbol}"
            AlertType.BELOW -> "📉 Alerta de Baixa - ${alert.symbol}"
        }
        
        val message = when (alert.alertType) {
            AlertType.ABOVE -> "${alert.symbol} atingiu R$ ${String.format("%.2f", currentPrice.price)} (meta: R$ ${String.format("%.2f", alert.targetPrice)})"
            AlertType.BELOW -> "${alert.symbol} caiu para R$ ${String.format("%.2f", currentPrice.price)} (meta: R$ ${String.format("%.2f", alert.targetPrice)})"
        }
        
        // Enviar notificação local
        notificationService.showPriceAlertNotification(title, message, alert.symbol)
        
        // Salvar histórico de alertas disparados
        saveAlertHistory(alert, currentPrice)
        
        Log.d("PriceAlertService", "Alerta disparado: $title - $message")
    }

    private suspend fun saveAlertHistory(alert: PriceAlert, currentPrice: StockPrice) {
        try {
            val historyRef = databaseService.database.getReference("alert_history")
            val historyEntry = mapOf(
                "alertId" to alert.id,
                "symbol" to alert.symbol,
                "targetPrice" to alert.targetPrice,
                "actualPrice" to currentPrice.price,
                "alertType" to alert.alertType.name,
                "triggeredAt" to System.currentTimeMillis(),
                "userId" to alert.userId
            )
            historyRef.push().setValue(historyEntry)
        } catch (e: Exception) {
            Log.e("PriceAlertService", "Erro ao salvar histórico: ${e.message}")
        }
    }

    // Buscar preços reais via API
    fun fetchRealStockPrice(symbol: String) {
        scope.launch {
            try {
                val stockPrice = stockPriceApiService.getStockPrice(symbol)
                if (stockPrice != null) {
                    currentPrices[symbol.uppercase()] = stockPrice
                    databaseService.updateStockPrice(stockPrice)
                    Log.d("PriceAlertService", "Preço real obtido: $symbol = R$ ${stockPrice.price}")
                    
                    // Verificar alertas após atualizar preço
                    checkAllAlerts()
                }
            } catch (e: Exception) {
                Log.e("PriceAlertService", "Erro ao buscar preço real: ${e.message}")
            }
        }
    }

    // Buscar preços de ações populares
    fun fetchPopularStocks() {
        scope.launch {
            try {
                val popularStocks = stockPriceApiService.getPopularBrazilianStocks()
                popularStocks.forEach { (symbol, price) ->
                    currentPrices[symbol] = price
                    databaseService.updateStockPrice(price)
                }
                Log.d("PriceAlertService", "Preços populares atualizados: ${popularStocks.size} ações")
                
                // Verificar alertas após atualizar preços
                checkAllAlerts()
            } catch (e: Exception) {
                Log.e("PriceAlertService", "Erro ao buscar ações populares: ${e.message}")
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        Log.d("PriceAlertService", "Monitoramento parado")
    }
} 