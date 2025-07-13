package com.example.investidorapp.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseMessagingService: FirebaseMessagingService() {

    private val databaseService = FirebaseDatabaseService()
    private val notificationService by lazy { NotificationService(this) }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FirebaseMessaging", "Mensagem recebida: ${remoteMessage.data}")
        
        // Processar dados da mensagem
        val messageType = remoteMessage.data["type"] ?: "general"
        
        when (messageType) {
            "price_alert" -> handlePriceAlertMessage(remoteMessage)
            "portfolio_update" -> handlePortfolioUpdateMessage(remoteMessage)
            "market_news" -> handleMarketNewsMessage(remoteMessage)
            else -> handleGeneralMessage(remoteMessage)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FirebaseMessaging", "Novo token: $token")
        
        // Salvar token no banco de dados
        scope.launch {
            try {
                // Em produção, você obteria o userId do sistema de autenticação
                val userId = "user_${System.currentTimeMillis()}"
                databaseService.saveUserToken(userId, token)
                Log.d("FirebaseMessaging", "Token salvo para usuário: $userId")
            } catch (e: Exception) {
                Log.e("FirebaseMessaging", "Erro ao salvar token: ${e.message}")
            }
        }
    }

    private fun handlePriceAlertMessage(remoteMessage: RemoteMessage) {
        val symbol = remoteMessage.data["symbol"] ?: ""
        val currentPrice = remoteMessage.data["current_price"]?.toDoubleOrNull() ?: 0.0
        val targetPrice = remoteMessage.data["target_price"]?.toDoubleOrNull() ?: 0.0
        val alertType = remoteMessage.data["alert_type"] ?: "ABOVE"
        
        val title = when (alertType) {
            "ABOVE" -> "🚀 Alerta de Alta - $symbol"
            "BELOW" -> "📉 Alerta de Baixa - $symbol"
            else -> "Alerta de Preço - $symbol"
        }
        
        val message = "$symbol atingiu R$ ${String.format("%.2f", currentPrice)} (meta: R$ ${String.format("%.2f", targetPrice)})"
        
        notificationService.showPriceAlertNotification(title, message, symbol)
    }

    private fun handlePortfolioUpdateMessage(remoteMessage: RemoteMessage) {
        val portfolioValue = remoteMessage.data["portfolio_value"]?.toDoubleOrNull() ?: 0.0
        val change = remoteMessage.data["change"]?.toDoubleOrNull() ?: 0.0
        val changePercent = remoteMessage.data["change_percent"]?.toDoubleOrNull() ?: 0.0
        
        notificationService.showPortfolioUpdateNotification(portfolioValue, change, changePercent)
    }

    private fun handleMarketNewsMessage(remoteMessage: RemoteMessage) {
        val newsTitle = remoteMessage.data["news_title"] ?: "Nova notícia do mercado"
        notificationService.showMarketNewsNotification(newsTitle)
    }

    private fun handleGeneralMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Nova Notificação"
        val message = remoteMessage.notification?.body ?: "Você recebeu uma nova mensagem."
        
        notificationService.showGeneralNotification(title, message)
    }
}