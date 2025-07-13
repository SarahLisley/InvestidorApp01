package com.example.investidorapp.service

import com.example.investidorapp.model.PriceAlert
import com.example.investidorapp.model.StockPrice
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class FirebaseDatabaseService {
    val database: FirebaseDatabase = Firebase.database
    private val alertsRef = database.getReference("price_alerts")
    private val pricesRef = database.getReference("stock_prices")
    private val usersRef = database.getReference("users")

    // Salvar alerta de preço
    suspend fun savePriceAlert(alert: PriceAlert): Result<String> {
        return try {
            val alertId = alertsRef.push().key ?: throw Exception("Erro ao gerar ID")
            val alertWithId = alert.copy(id = alertId)
            
            Log.d("FirebaseDatabaseService", "Salvando alerta: $alertWithId")
            
            alertsRef.child(alertId).setValue(alertWithId).await()
            Result.success(alertId)
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseService", "Erro ao salvar alerta: ${e.message}")
            Result.failure(e)
        }
    }

    // Obter alertas de um usuário
    fun getUserAlerts(userId: String): Flow<List<PriceAlert>> = callbackFlow {
        val listener = alertsRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alerts = mutableListOf<PriceAlert>()
                    for (child in snapshot.children) {
                        child.getValue(PriceAlert::class.java)?.let { alerts.add(it) }
                    }
                    trySend(alerts)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
        
        awaitClose { alertsRef.removeEventListener(listener) }
    }

    // Atualizar preço de uma ação
    suspend fun updateStockPrice(stockPrice: StockPrice): Result<Unit> {
        return try {
            pricesRef.child(stockPrice.symbol).setValue(stockPrice).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obter preço atual de uma ação
    suspend fun getStockPrice(symbol: String): StockPrice? {
        return try {
            val snapshot = pricesRef.child(symbol).get().await()
            snapshot.getValue(StockPrice::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Obter todos os alertas ativos
    fun getActiveAlerts(): Flow<List<PriceAlert>> = callbackFlow {
        val listener = alertsRef.orderByChild("active").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val alerts = mutableListOf<PriceAlert>()
                    for (child in snapshot.children) {
                        child.getValue(PriceAlert::class.java)?.let { alerts.add(it) }
                    }
                    trySend(alerts)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
        
        awaitClose { alertsRef.removeEventListener(listener) }
    }

    // Obter todos os alertas ativos de forma síncrona
    suspend fun getActiveAlertsSync(): List<PriceAlert> {
        return try {
            Log.d("FirebaseDatabaseService", "Buscando alertas ativos...")
            val snapshot = alertsRef.orderByChild("active").equalTo(true).get().await()
            val alerts = mutableListOf<PriceAlert>()
            
            Log.d("FirebaseDatabaseService", "Snapshot encontrado: ${snapshot.childrenCount} alertas")
            
            for (child in snapshot.children) {
                val alert = child.getValue(PriceAlert::class.java)
                if (alert != null) {
                    alerts.add(alert)
                    Log.d("FirebaseDatabaseService", "Alerta ativo encontrado: ${alert.symbol} - Preço alvo: ${alert.targetPrice} - Ativo: ${alert.active}")
                } else {
                    Log.w("FirebaseDatabaseService", "Falha ao converter alerta: ${child.key}")
                }
            }
            
            Log.d("FirebaseDatabaseService", "Total de alertas ativos: ${alerts.size}")
            alerts
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseService", "Erro ao buscar alertas ativos: ${e.message}")
            emptyList()
        }
    }

    // Desativar alerta
    suspend fun deactivateAlert(alertId: String): Result<Unit> {
        return try {
            alertsRef.child(alertId).child("active").setValue(false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Excluir alerta
    suspend fun deleteAlert(alertId: String): Result<Unit> {
        return try {
            alertsRef.child(alertId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Salvar token do usuário
    suspend fun saveUserToken(userId: String, token: String): Result<Unit> {
        return try {
            usersRef.child(userId).child("fcmToken").setValue(token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obter token de um usuário
    suspend fun getUserToken(userId: String): String? {
        return try {
            val snapshot = usersRef.child(userId).child("fcmToken").get().await()
            snapshot.getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
    }
} 