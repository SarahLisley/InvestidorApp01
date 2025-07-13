package com.example.investidorapp.model

data class PriceAlert(
    val id: String = "",
    val symbol: String = "",
    val currentPrice: Double = 0.0,
    val targetPrice: Double = 0.0,
    val alertType: AlertType = AlertType.ABOVE,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
)

enum class AlertType {
    ABOVE,  // Alerta quando preço subir acima do valor
    BELOW   // Alerta quando preço cair abaixo do valor
}

data class StockPrice(
    val symbol: String = "",
    val price: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) 