package com.example.investidorapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.investidorapp.model.AlertType
import com.example.investidorapp.model.PriceAlert
import com.example.investidorapp.model.StockPrice
import com.example.investidorapp.service.FirebaseDatabaseService
import com.example.investidorapp.service.NotificationService
import com.example.investidorapp.service.PriceAlertService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class PriceAlertViewModel(application: Application) : AndroidViewModel(application) {
    
    private val databaseService = FirebaseDatabaseService()
    private val notificationService = NotificationService(application)
    private val priceAlertService = PriceAlertService(databaseService, notificationService)
    
    // UserId fixo para garantir consistência
    private val userId = "test_user_001"
    
    private val _alerts = MutableStateFlow<List<PriceAlert>>(emptyList())
    val alerts: StateFlow<List<PriceAlert>> = _alerts.asStateFlow()
    
    private val _stockPrices = MutableStateFlow<Map<String, StockPrice>>(emptyMap())
    val stockPrices: StateFlow<Map<String, StockPrice>> = _stockPrices.asStateFlow()
    
    private val _isLoadingAlerts = MutableStateFlow(false)
    val isLoadingAlerts: StateFlow<Boolean> = _isLoadingAlerts.asStateFlow()

    private val _isLoadingPrices = MutableStateFlow(false)
    val isLoadingPrices: StateFlow<Boolean> = _isLoadingPrices.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            startMonitoring()
            loadUserAlerts()
        }
    }

    private fun startMonitoring() {
        priceAlertService.startMonitoring()
    }

    private fun loadUserAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoadingAlerts.value = true
                
                databaseService.getUserAlerts(userId).collect { alertsList ->
                    _alerts.value = alertsList
                    Log.d("PriceAlertViewModel", "Alertas carregados: ${alertsList.size}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar alertas: ${e.message}"
            } finally {
                _isLoadingAlerts.value = false
            }
        }
    }

    fun createPriceAlert(
        symbol: String,
        targetPrice: Double,
        alertType: AlertType,
        currentPrice: Double = 0.0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoadingAlerts.value = true
                
                val alert = PriceAlert(
                    symbol = symbol.uppercase(),
                    currentPrice = currentPrice,
                    targetPrice = targetPrice,
                    alertType = alertType,
                    userId = userId
                )
                
                val result = databaseService.savePriceAlert(alert)
                result.fold(
                    onSuccess = { alertId ->
                        // Alerta criado com sucesso
                        _errorMessage.value = "Alerta criado com sucesso! ID: $alertId"
                        Log.d("PriceAlertViewModel", "Alerta criado: $alertId")
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Erro ao criar alerta: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao criar alerta: ${e.message}"
            } finally {
                _isLoadingAlerts.value = false
            }
        }
    }

    /**
     * Test01: Função para adaptar o valor do alerta para facilitar testes
     * Modifica o preço alvo para um valor mais próximo do preço atual
     */
    fun createTest01PriceAlert(
        symbol: String,
        targetPrice: Double,
        alertType: AlertType
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoadingAlerts.value = true
                
                // Buscar o preço atual da ação
                val stockPriceApiService = com.example.investidorapp.service.StockPriceApiService()
                val currentStockPrice = stockPriceApiService.getStockPrice(symbol.uppercase())
                
                if (currentStockPrice != null) {
                    val currentPrice = currentStockPrice.price
                    
                    // Adaptar o preço alvo para facilitar o teste
                    val adaptedTargetPrice = when (alertType) {
                        AlertType.ABOVE -> {
                            // Para alerta "acima de", definir preço alvo ligeiramente acima do atual
                            currentPrice + (currentPrice * 0.01) // +1% do preço atual
                        }
                        AlertType.BELOW -> {
                            // Para alerta "abaixo de", definir preço alvo ligeiramente abaixo do atual
                            currentPrice - (currentPrice * 0.01) // -1% do preço atual
                        }
                    }
                    
                    // Criar o alerta com o preço adaptado
                    val alert = PriceAlert(
                        symbol = symbol.uppercase(),
                        currentPrice = currentPrice,
                        targetPrice = adaptedTargetPrice,
                        alertType = alertType,
                        userId = userId
                    )
                    
                    val result = databaseService.savePriceAlert(alert)
                    result.fold(
                        onSuccess = { alertId ->
                            _errorMessage.value = "Test01: Alerta criado com preço adaptado! Preço atual: R$ ${String.format("%.2f", currentPrice)}, Preço alvo: R$ ${String.format("%.2f", adaptedTargetPrice)}"
                        },
                        onFailure = { exception ->
                            _errorMessage.value = "Erro ao criar alerta Test01: ${exception.message}"
                        }
                    )
                } else {
                    _errorMessage.value = "Test01: Não foi possível obter o preço atual de $symbol"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro no Test01: ${e.message}"
            } finally {
                _isLoadingAlerts.value = false
            }
        }
    }

    /**
     * Simula mudanças de preço para testar alertas Test01
     * Útil para testar se os alertas estão funcionando corretamente
     */
    fun simulatePriceChangeForTest01(symbol: String, percentageChange: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Buscar o preço atual
                val stockPriceApiService = com.example.investidorapp.service.StockPriceApiService()
                val currentStockPrice = stockPriceApiService.getStockPrice(symbol.uppercase())
                
                if (currentStockPrice != null) {
                    val currentPrice = currentStockPrice.price
                    val newPrice = currentPrice + (currentPrice * percentageChange / 100)
                    
                    // Criar um novo StockPrice com o preço simulado
                    val simulatedStockPrice = StockPrice(
                        symbol = symbol.uppercase(),
                        price = newPrice,
                        change = newPrice - currentPrice,
                        changePercent = percentageChange,
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    // Atualizar o preço no Firebase para disparar alertas
                    databaseService.updateStockPrice(simulatedStockPrice)
                    
                    // Atualizar a UI
                    _stockPrices.value = _stockPrices.value + (symbol.uppercase() to simulatedStockPrice)
                    
                    _errorMessage.value = "Test01: Preço simulado para $symbol - R$ ${String.format("%.2f", newPrice)} (${if (percentageChange >= 0) "+" else ""}${String.format("%.2f", percentageChange)}%)"
                    
                    // Verificar alertas após simular o preço
                    Log.d("PriceAlertViewModel", "Verificando alertas após simulação de preço...")
                    priceAlertService.checkAllAlerts()
                    
                } else {
                    _errorMessage.value = "Test01: Não foi possível obter o preço atual de $symbol"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao simular mudança de preço: ${e.message}"
            }
        }
    }

    /**
     * Limpa o cache da API para forçar novas requisições
     */
    fun clearApiCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stockPriceApiService = com.example.investidorapp.service.StockPriceApiService()
                stockPriceApiService.clearCache()
                _errorMessage.value = "Cache da API limpo com sucesso!"
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao limpar cache: ${e.message}"
            }
        }
    }

    fun deactivateAlert(alertId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = databaseService.deactivateAlert(alertId)
                result.fold(
                    onSuccess = {
                        // Alerta desativado com sucesso
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Erro ao desativar alerta: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao desativar alerta: ${e.message}"
            }
        }
    }

    fun getStockPrice(symbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoadingPrices.value = true
                
                // Buscar preço diretamente da API
                val stockPriceApiService = com.example.investidorapp.service.StockPriceApiService()
                val stockPrice = stockPriceApiService.getStockPrice(symbol.uppercase())
                
                if (stockPrice != null) {
                    // Atualizar UI imediatamente
                    _stockPrices.value = _stockPrices.value + (symbol.uppercase() to stockPrice)
                    // Salvar no Firebase em background
                    databaseService.updateStockPrice(stockPrice)
                } else {
                    _errorMessage.value = "Não foi possível obter preço para $symbol"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao obter preço: ${e.message}"
            } finally {
                _isLoadingPrices.value = false
            }
        }
    }

    // Buscar preços de ações populares de forma otimizada
    fun fetchPopularStocks() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingPrices.value = true
            _errorMessage.value = null
            try {
                val stockPriceApiService = com.example.investidorapp.service.StockPriceApiService()
                val popularStocks = listOf(
                    "PETR4", "VALE3", "ITUB4", "BBDC4", "ABEV3",
                    "WEGE3", "RENT3", "LREN3", "MGLU3", "JBSS3"
                )

                // Usar a função otimizada para buscar preços em paralelo
                val prices = stockPriceApiService.getMultipleStockPrices(popularStocks)

                if (prices.isNotEmpty()) {
                    // Atualizar a UI com os novos preços
                    _stockPrices.value = prices
                    Log.d("PriceAlertViewModel", "Preços populares carregados: ${prices.size} ações")

                    // Salvar os preços atualizados no Firebase em background
                    viewModelScope.launch(Dispatchers.IO) {
                        prices.values.forEach { stockPrice ->
                            databaseService.updateStockPrice(stockPrice)
                        }
                    }
                } else {
                    _errorMessage.value = "Não foi possível buscar os preços das ações. Verifique sua conexão com a internet."
                    Log.w("PriceAlertViewModel", "Nenhum preço foi retornado pela API")
                }

            } catch (e: Exception) {
                _errorMessage.value = "Erro ao buscar ações populares: ${e.message}"
                Log.e("PriceAlertViewModel", "Erro ao buscar preços populares", e)
            } finally {
                _isLoadingPrices.value = false
                Log.d("PriceAlertViewModel", "Busca de preços populares finalizada")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Recarrega os alertas do usuário
     */
    fun reloadAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoadingAlerts.value = true
                Log.d("PriceAlertViewModel", "Recarregando alertas para userId: $userId")
                
                databaseService.getUserAlerts(userId).collect { alertsList ->
                    _alerts.value = alertsList
                    Log.d("PriceAlertViewModel", "Alertas recarregados: ${alertsList.size}")
                    _errorMessage.value = "Alertas recarregados: ${alertsList.size} encontrados"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao recarregar alertas: ${e.message}"
                Log.e("PriceAlertViewModel", "Erro ao recarregar alertas", e)
            } finally {
                _isLoadingAlerts.value = false
            }
        }
    }

    /**
     * Verifica alertas manualmente
     */
    fun checkAlertsManually() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoadingAlerts.value = true
                Log.d("PriceAlertViewModel", "Verificação manual de alertas iniciada")
                
                // Chamar verificação manual no PriceAlertService
                priceAlertService.checkAllAlerts()
                
                _errorMessage.value = "Verificação de alertas iniciada"
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao verificar alertas: ${e.message}"
                Log.e("PriceAlertViewModel", "Erro ao verificar alertas", e)
            } finally {
                _isLoadingAlerts.value = false
            }
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = databaseService.deleteAlert(alertId)
                result.fold(
                    onSuccess = {
                        _errorMessage.value = "Alerta excluído com sucesso!"
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Erro ao excluir alerta: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao excluir alerta: ${e.message}"
            }
        }
    }

    fun editAlert(alert: PriceAlert, newTargetPrice: Double, newAlertType: AlertType) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedAlert = alert.copy(targetPrice = newTargetPrice, alertType = newAlertType)
                val result = databaseService.savePriceAlert(updatedAlert)
                result.fold(
                    onSuccess = {
                        _errorMessage.value = "Alerta editado com sucesso!"
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Erro ao editar alerta: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao editar alerta: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        priceAlertService.stopMonitoring()
    }
} 