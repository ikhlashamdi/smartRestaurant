package com.example.smartshop.uiLayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smartshop.data.Product

@Composable
fun ProductFormWithUrl(
    product: Product? = null,
    onSave: (Product) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "0") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "0.0") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Aperçu de l'image
        if (imageUrl.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Aperçu de l'image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = {
                        // Afficher icône si erreur de chargement
                    }
                )
            }
        }

        // Champ URL de l'image
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("URL de l'image") },
            placeholder = { Text("https://exemple.com/image.jpg") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Image, contentDescription = null)
            }
        )

        Text(
            text = "💡 Astuce : Utilisez Imgur, Unsplash ou toute autre URL d'image publique",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom du plat") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { text ->
                if (text.isEmpty() || text.isDigitsOnly()) {
                    quantity = text
                }
            },
            label = { Text("Quantité") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = price,
            onValueChange = { text ->
                if (text.isEmpty() || text.matches(Regex("^\\d*\\.?\\d*$"))) {
                    price = text
                }
            },
            label = { Text("Prix (€)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Annuler")
            }

            Button(
                onClick = {
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    val quantityValue = quantity.toIntOrNull() ?: 0

                    if (name.isNotBlank() && priceValue > 0 && quantityValue >= 0) {
                        val newProduct = Product(
                            id = product?.id ?: "",
                            name = name.trim(),
                            quantity = quantityValue,
                            price = priceValue,
                            userId = product?.userId ?: "",
                            imageUrl = imageUrl.trim()
                        )
                        onSave(newProduct)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() &&
                        (price.toDoubleOrNull() ?: 0.0) > 0 &&
                        (quantity.toIntOrNull() ?: 0) >= 0
            ) {
                Text(if (product == null) "Ajouter" else "Modifier")
            }
        }
    }
}