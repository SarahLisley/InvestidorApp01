package com.example.investidorapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.investidorapp.model.AlertType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, targetPrice: Double, alertType: AlertType) -> Unit,
    onTest01: (symbol: String, targetPrice: Double, alertType: AlertType) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var targetPriceText by remember { mutableStateOf("") }
    var selectedAlertType by remember { mutableStateOf(AlertType.ABOVE) }
    var isError by remember { mutableStateOf(false) }
    var useTest01 by remember { mutableStateOf(false) }

    // Use derivedStateOf to avoid unnecessary recompositions
    val symbolError by remember { derivedStateOf { isError && symbol.isEmpty() } }
    val priceError by remember { derivedStateOf { isError && (targetPriceText.isEmpty() || targetPriceText.toDoubleOrNull() == null) } }
    
    // Memoize the alert description to avoid recomposition
    val alertDescription by remember(selectedAlertType, targetPriceText, useTest01) {
        derivedStateOf {
            val baseDescription = when (selectedAlertType) {
                AlertType.ABOVE -> "🚀 Alerta será disparado quando o preço subir acima de R$ ${targetPriceText.ifEmpty { "0,00" }}"
                AlertType.BELOW -> "📉 Alerta será disparado quando o preço cair abaixo de R$ ${targetPriceText.ifEmpty { "0,00" }}"
            }
            
            if (useTest01) {
                "$baseDescription\n🧪 [MODO TESTE] Preço será adaptado automaticamente!"
            } else {
                baseDescription
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Alerta de Preço") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Campo para símbolo da ação
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { 
                        symbol = it.uppercase()
                        isError = false
                    },
                    label = { Text("Símbolo (ex: PETR4)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = symbolError,
                    supportingText = {
                        if (symbolError) {
                            Text("Símbolo é obrigatório")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para preço alvo
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { 
                        targetPriceText = it
                        isError = false
                    },
                    label = { Text("Preço Alvo (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = priceError,
                    supportingText = {
                        when {
                            isError && targetPriceText.isEmpty() -> Text("Preço é obrigatório")
                            isError && targetPriceText.toDoubleOrNull() == null -> Text("Preço deve ser um número válido")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seleção do tipo de alerta
                Text(
                    text = "Tipo de Alerta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlertType.values().forEach { alertType ->
                        FilterChip(
                            selected = selectedAlertType == alertType,
                            onClick = { selectedAlertType = alertType },
                            label = {
                                Text(
                                    when (alertType) {
                                        AlertType.ABOVE -> "Acima de"
                                        AlertType.BELOW -> "Abaixo de"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opção Test01
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (useTest01) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = useTest01,
                            onCheckedChange = { useTest01 = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "🧪 Modo Teste (Test01)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Adapta automaticamente o preço alvo para facilitar testes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Informação sobre o tipo de alerta
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = alertDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (symbol.isNotEmpty() && targetPriceText.isNotEmpty()) {
                        val targetPrice = targetPriceText.toDoubleOrNull()
                        if (targetPrice != null && targetPrice > 0) {
                            if (useTest01) {
                                onTest01(symbol, targetPrice, selectedAlertType)
                            } else {
                                onConfirm(symbol, targetPrice, selectedAlertType)
                            }
                        } else {
                            isError = true
                        }
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(if (useTest01) "Criar Alerta Test01" else "Criar Alerta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 