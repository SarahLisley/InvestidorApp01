package com.example.investidorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.investidorapp.model.AlertType
import com.example.investidorapp.model.PriceAlert
import com.example.investidorapp.ui.components.AddAlertDialog
import com.example.investidorapp.viewmodel.PriceAlertViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHost

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    val viewModel: PriceAlertViewModel = viewModel()
    val alerts by viewModel.alerts.collectAsState()
    val stockPrices by viewModel.stockPrices.collectAsState()
    val isLoadingAlerts by viewModel.isLoadingAlerts.collectAsState()
    val isLoadingPrices by viewModel.isLoadingPrices.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showAddAlertDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Chamar fetchPopularStocks automaticamente ao abrir a aba de preços
    var alreadyFetched by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 && stockPrices.isEmpty() && !isLoadingPrices && !alreadyFetched) {
            alreadyFetched = true
            viewModel.fetchPopularStocks()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InvestidorApp") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAlertDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Alerta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Alertas") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Preços") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) {
                page ->
                when (page) {
                    0 -> AlertsTab(alerts, isLoadingAlerts, viewModel)
                    1 -> PricesTab(viewModel, isLoadingPrices, alreadyFetched, onResetAlreadyFetched = { alreadyFetched = it })
                }
            }
        }

        // Dialog para adicionar alerta
        if (showAddAlertDialog) {
            AddAlertDialog(
                onDismiss = { showAddAlertDialog = false },
                onConfirm = { symbol, targetPrice, alertType ->
                    viewModel.createPriceAlert(symbol, targetPrice, alertType)
                    showAddAlertDialog = false
                },
                onTest01 = { symbol, targetPrice, alertType ->
                    viewModel.createTest01PriceAlert(symbol, targetPrice, alertType)
                    showAddAlertDialog = false
                }
            )
        }

        // Snackbar para erros
        errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Mostrar Snackbar
            }
        }
    }
}

@Composable
fun AlertsTab(
    alerts: List<PriceAlert>,
    isLoading: Boolean,
    viewModel: PriceAlertViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    var editingAlert by remember { mutableStateOf<PriceAlert?>(null) }
    var newTargetPrice by remember { mutableStateOf(0.0) }
    var newAlertType by remember { mutableStateOf(AlertType.ABOVE) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Card explicativo do modo Teste
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🧪 Modo Teste Rápido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crie um alerta em modo teste. Depois, use os botões +2% ou -2% para simular o preço e disparar o alerta instantaneamente.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            // Botão para recarregar alertas
            item {
                Button(
                    onClick = { viewModel.reloadAlerts() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(" Recarregar Alertas")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { 
                        // Chamar verificação manual de alertas
                        viewModel.checkAlertsManually()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("🔍 Verificar Alertas Agora")
                }
            }
            
            if (alerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum alerta configurado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Toque no + para criar seu primeiro alerta",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = alerts,
                    key = { it.id } // Add key for better performance
                ) { alert ->
                    AlertCard(
                        alert = alert,
                        onDeactivate = { viewModel.deactivateAlert(alert.id) },
                        onEdit = {
                            editingAlert = it
                            newTargetPrice = it.targetPrice
                            newAlertType = it.alertType
                        },
                        onDelete = { viewModel.deleteAlert(it.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        // Diálogo de edição
        if (editingAlert != null) {
            AlertEditDialog(
                alert = editingAlert!!,
                targetPrice = newTargetPrice,
                alertType = newAlertType,
                onTargetPriceChange = { newTargetPrice = it },
                onAlertTypeChange = { newAlertType = it },
                onDismiss = { editingAlert = null },
                onConfirm = {
                    viewModel.editAlert(editingAlert!!, newTargetPrice, newAlertType)
                    editingAlert = null
                }
            )
        }
    }
}

@Composable
fun AlertCard(
    alert: PriceAlert,
    onDeactivate: () -> Unit,
    onEdit: (PriceAlert) -> Unit,
    onDelete: (PriceAlert) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.active) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { onEdit(alert) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { onDelete(alert) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Preço Alvo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "R$ ${String.format("%.2f", alert.targetPrice)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tipo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (alert.alertType) {
                            AlertType.ABOVE -> "Acima de"
                            AlertType.BELOW -> "Abaixo de"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (alert.alertType) {
                            AlertType.ABOVE -> MaterialTheme.colorScheme.primary
                            AlertType.BELOW -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Criado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(alert.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (alert.active) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDeactivate,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Desativar Alerta")
                }
            }
        }
    }
}

@Composable
fun AlertEditDialog(
    alert: PriceAlert,
    targetPrice: Double,
    alertType: AlertType,
    onTargetPriceChange: (Double) -> Unit,
    onAlertTypeChange: (AlertType) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Alerta de ${alert.symbol}") },
        text = {
            Column {
                OutlinedTextField(
                    value = targetPrice.toString(),
                    onValueChange = { v -> v.toDoubleOrNull()?.let { onTargetPriceChange(it) } },
                    label = { Text("Novo Preço Alvo") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    RadioButton(
                        selected = alertType == AlertType.ABOVE,
                        onClick = { onAlertTypeChange(AlertType.ABOVE) }
                    )
                    Text("Acima de")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = alertType == AlertType.BELOW,
                        onClick = { onAlertTypeChange(AlertType.BELOW) }
                    )
                    Text("Abaixo de")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PricesTab(viewModel: PriceAlertViewModel, isLoading: Boolean, alreadyFetched: Boolean, onResetAlreadyFetched: (Boolean) -> Unit) {
    val stockPrices by viewModel.stockPrices.collectAsState()
    val isLoadingPrices by viewModel.isLoadingPrices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Preços em Tempo Real",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para buscar preços populares
        Button(
            onClick = { 
                onResetAlreadyFetched(false) // Resetar flag para permitir nova busca
                viewModel.fetchPopularStocks() 
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingPrices
        ) {
            Text("Atualizar Ações Populares")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Seção de Testes Test01
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🧪 Testes Test01",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Simular mudanças de preço para testar alertas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            stockPrices.keys.firstOrNull()?.let { symbol ->
                                viewModel.simulatePriceChangeForTest01(symbol, 2.0) // +2%
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = stockPrices.isNotEmpty()
                    ) {
                        Text("+2%")
                    }
                    
                    Button(
                        onClick = { 
                            stockPrices.keys.firstOrNull()?.let { symbol ->
                                viewModel.simulatePriceChangeForTest01(symbol, -2.0) // -2%
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = stockPrices.isNotEmpty()
                    ) {
                        Text("-2%")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.clearApiCache() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("🗑️ Limpar Cache da API")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // A LazyColumn agora ocupa o espaço restante, corrigindo o problema de performance.
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoadingPrices && stockPrices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!isLoadingPrices && stockPrices.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum preço encontrado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Toque em 'Atualizar Ações Populares' para buscar os preços",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(
                items = stockPrices.toList(),
                key = { (symbol, _) -> symbol } // Add key for better performance
            ) { (symbol, price) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Atualizado: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(price.lastUpdated))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "R$ ${String.format("%.2f", price.price)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (price.change >= 0) "+" else ""}${String.format("%.2f", price.changePercent)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (price.change >= 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
} 