package com.example.investidorapp.service

import android.util.Log
import com.example.investidorapp.model.StockPrice
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class StockPriceApiService {
    
    companion object {
        private const val TAG = "StockPriceApiService"
        private const val BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val CACHE_DURATION_MS = 30000L // 30 segundos de cache
    }

    // Cache para armazenar preços recentes
    private val priceCache = mutableMapOf<String, CachedPrice>()
    
    private data class CachedPrice(
        val stockPrice: StockPrice,
        val timestamp: Long
    ) {
        fun isValid(): Boolean {
            return System.currentTimeMillis() - timestamp < CACHE_DURATION_MS
        }
    }

    suspend fun getStockPrice(symbol: String): StockPrice? = withContext(Dispatchers.IO) {
        try {
            val upperSymbol = symbol.uppercase()
            
            // Verificar cache primeiro
            val cachedPrice = priceCache[upperSymbol]
            if (cachedPrice != null && cachedPrice.isValid()) {
                Log.d(TAG, "Usando preço em cache para $upperSymbol: R$ ${String.format("%.2f", cachedPrice.stockPrice.price)}")
                return@withContext cachedPrice.stockPrice
            }
            
            // Se não há cache válido, buscar da API
            Log.d(TAG, "Cache expirado ou inexistente para $upperSymbol, buscando da API...")
            
            // Primeiro tentar com Yahoo Finance
            val yahooResult = getStockPriceFromYahoo(upperSymbol)
            if (yahooResult != null) {
                // Armazenar no cache
                priceCache[upperSymbol] = CachedPrice(yahooResult, System.currentTimeMillis())
                return@withContext yahooResult
            }
            
            // Se falhar, tentar com API alternativa
            Log.w(TAG, "Yahoo Finance falhou para $upperSymbol, tentando API alternativa...")
            val alternativeResult = getStockPriceFromAlternative(upperSymbol)
            if (alternativeResult != null) {
                // Armazenar no cache
                priceCache[upperSymbol] = CachedPrice(alternativeResult, System.currentTimeMillis())
            }
            return@withContext alternativeResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar preço para $symbol: ${e.message}")
            null
        }
    }
    
    private suspend fun getStockPriceFromYahoo(symbol: String): StockPrice? = withContext(Dispatchers.IO) {
        try {
            // Adicionar .SA para ações brasileiras
            val brazilianSymbol = if (!symbol.endsWith(".SA")) "$symbol.SA" else symbol
            val url = "$BASE_URL$brazilianSymbol?interval=1d&range=1d"
            
            Log.d(TAG, "Buscando preço no Yahoo Finance para: $brazilianSymbol")
            
            val connection = URL(url).openConnection().apply {
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)
            
            Log.d(TAG, "Resposta da API para $symbol: ${response.take(200)}...")
            
            // Verificar se há dados
            val chart = jsonObject.getJSONObject("chart")
            val result = chart.getJSONArray("result")
            
            if (result.length() == 0) {
                Log.w(TAG, "Nenhum resultado encontrado para $symbol")
                return@withContext null
            }
            
            val firstResult = result.getJSONObject(0)
            val meta = firstResult.getJSONObject("meta")
            
            // Verificar se há timestamp (pode estar vazio quando mercado fechado)
            val timestamp = try {
                firstResult.getJSONArray("timestamp")
            } catch (e: Exception) {
                Log.w(TAG, "timestamp não encontrado para $symbol")
                null
            }
            
            val indicators = firstResult.getJSONObject("indicators")
            val quote = indicators.getJSONArray("quote").getJSONObject(0)
            
            // Obter preços com tratamento de erros
            val currentPrice = meta.getDouble("regularMarketPrice")
            
            // Tentar obter previousClose, se não existir usar currentPrice
            val previousClose = try {
                meta.getDouble("previousClose")
            } catch (e: Exception) {
                Log.w(TAG, "previousClose não encontrado para $symbol, usando currentPrice")
                currentPrice
            }
            
            val change = currentPrice - previousClose
            val changePercent = if (previousClose > 0) (change / previousClose) * 100 else 0.0
            
            val stockPrice = StockPrice(
                symbol = symbol.uppercase(),
                price = currentPrice,
                change = change,
                changePercent = changePercent,
                lastUpdated = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Preço obtido do Yahoo Finance para $symbol: R$ ${String.format("%.2f", currentPrice)}")
            stockPrice
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro no Yahoo Finance para $symbol: ${e.message}")
            null
        }
    }
    
    private suspend fun getStockPriceFromAlternative(symbol: String): StockPrice? = withContext(Dispatchers.IO) {
        try {
            // Simular preços para demonstração (em produção, usar outra API)
            val simulatedPrices = mapOf(
                "PETR4" to 35.50,
                "VALE3" to 68.20,
                "ITUB4" to 32.15,
                "BBDC4" to 15.80,
                "ABEV3" to 12.45,
                "WEGE3" to 38.90,
                "RENT3" to 45.60,
                "LREN3" to 18.75,
                "MGLU3" to 2.85,
                "JBSS3" to 22.40
            )
            
            val price = simulatedPrices[symbol.uppercase()]
            if (price != null) {
                val stockPrice = StockPrice(
                    symbol = symbol.uppercase(),
                    price = price,
                    change = 0.0,
                    changePercent = 0.0,
                    lastUpdated = System.currentTimeMillis()
                )
                
                Log.d(TAG, "Preço simulado para $symbol: R$ ${String.format("%.2f", price)}")
                return@withContext stockPrice
            }
            
            Log.w(TAG, "Símbolo $symbol não encontrado na lista simulada")
            null
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro na API alternativa para $symbol: ${e.message}")
            null
        }
    }

    suspend fun getMultipleStockPrices(symbols: List<String>): Map<String, StockPrice> = coroutineScope {
        val priceJobs = symbols.map { symbol ->
            async {
                getStockPrice(symbol)
            }
        }
        priceJobs.awaitAll()
            .filterNotNull()
            .associateBy { it.symbol.uppercase() }
    }

    // Buscar preços de ações brasileiras populares
    suspend fun getPopularBrazilianStocks(): Map<String, StockPrice> {
        val popularStocks = listOf(
            "PETR4", "VALE3", "ITUB4", "BBDC4", "ABEV3", 
            "WEGE3", "RENT3", "LREN3", "MGLU3", "JBSS3"
        )
        
        return getMultipleStockPrices(popularStocks)
    }
    
    // Função para limpar cache (útil para testes)
    fun clearCache() {
        priceCache.clear()
        Log.d(TAG, "Cache limpo")
    }
    
    // Função para forçar atualização (ignora cache)
    suspend fun forceRefreshStockPrice(symbol: String): StockPrice? = withContext(Dispatchers.IO) {
        val upperSymbol = symbol.uppercase()
        
        // Remover do cache para forçar nova busca
        priceCache.remove(upperSymbol)
        
        // Buscar novamente
        getStockPrice(upperSymbol)
    }
} 