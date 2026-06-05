package com.example.e_commerce_cm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.e_commerce_cm.viewmodel.CartViewModel
import com.example.e_commerce_cm.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    checkoutViewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val total = cartViewModel.getTotal()

    var fullName    by remember { mutableStateOf("") }
    var phone       by remember { mutableStateOf("") }
    var address     by remember { mutableStateOf("") }
    var city        by remember { mutableStateOf("") }
    var department  by remember { mutableStateOf("") }
    var notes       by remember { mutableStateOf("") }

    var showErrors   by remember { mutableStateOf(false) }
    var showSuccess  by remember { mutableStateOf(false) }

    fun isValid() = fullName.isNotBlank() && phone.isNotBlank() &&
            address.isNotBlank() && city.isNotBlank() && department.isNotBlank()

    if (showSuccess) {
        OrderSuccessDialog(
            total = total,
            onDismiss = {
                cartViewModel.clearCart()
                onOrderPlaced()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos de entrega") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Resumen del pedido
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${cartItems.sumOf { it.quantity }} producto(s)",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            "Total: $${String.format("%.2f", total)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Pago contra entrega",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            "Gratis",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (isValid()) {
                                checkoutViewModel.placeOrder(
                                    fullName   = fullName,
                                    phone      = phone,
                                    address    = address,
                                    city       = city,
                                    department = department,
                                    notes      = notes,
                                    total      = total
                                )
                                showSuccess = true
                            } else {
                                showErrors = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Confirmar pedido", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección: Información personal
            SectionHeader(icon = Icons.Filled.Person, title = "Información personal")

            DeliveryTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Nombre completo",
                placeholder = "Ej. Juan Pérez",
                isError = showErrors && fullName.isBlank(),
                errorMsg = "El nombre es requerido",
                keyboardType = KeyboardType.Text
            )

            DeliveryTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() } },
                label = "Teléfono / Celular",
                placeholder = "Ej. 3001234567",
                isError = showErrors && phone.isBlank(),
                errorMsg = "El teléfono es requerido",
                keyboardType = KeyboardType.Phone,
                leadingIcon = {
                    Text("+57 ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            )

            Spacer(Modifier.height(4.dp))

            // Sección: Dirección de entrega
            SectionHeader(icon = Icons.Filled.LocationOn, title = "Dirección de entrega")

            DeliveryTextField(
                value = address,
                onValueChange = { address = it },
                label = "Dirección",
                placeholder = "Ej. Cra 50 #12-34, Apto 201",
                isError = showErrors && address.isBlank(),
                errorMsg = "La dirección es requerida",
                keyboardType = KeyboardType.Text
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DeliveryTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "Ciudad",
                    placeholder = "Ej. Medellín",
                    isError = showErrors && city.isBlank(),
                    errorMsg = "Requerida",
                    modifier = Modifier.weight(1f)
                )
                DeliveryTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = "Departamento",
                    placeholder = "Ej. Antioquia",
                    isError = showErrors && department.isBlank(),
                    errorMsg = "Requerido",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Notas opcionales
            SectionHeader(icon = Icons.Filled.Edit, title = "Notas (opcional)")

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Instrucciones adicionales") },
                placeholder = { Text("Ej. Timbre 2, portería sur, horario de entrega...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )

            // Info contra entrega
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Pagarás en efectivo al recibir tu pedido. El mensajero llegará a tu dirección en 2-5 días hábiles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun DeliveryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMsg: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = leadingIcon,
            singleLine = true
        )
        if (isError && errorMsg.isNotBlank()) {
            Text(
                errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun OrderSuccessDialog(total: Double, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "¡Pedido confirmado!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "Tu pedido por $${String.format("%.2f", total)} fue registrado exitosamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "Pagarás al recibir tu pedido en 2-5 días hábiles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}