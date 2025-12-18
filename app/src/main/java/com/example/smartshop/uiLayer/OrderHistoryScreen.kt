package com.example.smartshop.uiLayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshop.ViewModel.CartViewModel
import com.example.smartshop.data.Order
import com.example.smartshop.data.OrderItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: CartViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Commandes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Aucune commande",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Vos commandes apparaîtront ici",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onStatusChange = { newStatus ->
                            viewModel.updateOrderStatus(order, newStatus)
                        },
                        onDelete = { viewModel.deleteOrder(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Décoder les items de la commande
    val orderItems: List<OrderItem> = try {
        val type = object : TypeToken<List<OrderItem>>() {}.type
        Gson().fromJson(order.items, type)
    } catch (e: Exception) {
        emptyList()
    }

    // Formater la date
    val dateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
    val dateString = dateFormat.format(Date(order.orderDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (order.status) {
                "Livrée" -> MaterialTheme.colorScheme.tertiaryContainer
                "Annulée" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Commande #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                StatusChip(
                    status = order.status,
                    onClick = { showStatusDialog = true }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", fontWeight = FontWeight.Bold)
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(order.totalAmount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bouton pour voir les détails
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Masquer les détails" else "Voir les détails")
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            // Détails de la commande
            if (expanded) {
                Divider()
                Spacer(Modifier.height(8.dp))

                orderItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} x${item.quantity}")
                        Text(
                            NumberFormat.getCurrencyInstance(Locale.FRANCE).format(item.price * item.quantity)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Bouton supprimer
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Supprimer la commande")
                }
            }
        }
    }

    // Dialogue de changement de statut
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Changer le statut") },
            text = {
                Column {
                    listOf("En cours", "Livrée", "Annulée").forEach { status ->
                        TextButton(
                            onClick = {
                                onStatusChange(status)
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(status)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialogue de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la commande") },
            text = { Text("Voulez-vous vraiment supprimer cette commande ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun StatusChip(status: String, onClick: () -> Unit) {
    val (icon, color) = when (status) {
        "Livrée" -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.tertiary
        "Annulée" -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
        else -> Icons.Default.Schedule to MaterialTheme.colorScheme.primary
    }

    AssistChip(
        onClick = onClick,
        label = { Text(status) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        colors = AssistChipDefaults.assistChipColors(
            leadingIconContentColor = color
        )
    )
}