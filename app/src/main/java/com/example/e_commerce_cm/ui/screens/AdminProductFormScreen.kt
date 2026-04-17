package com.example.e_commerce_cm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.e_commerce_cm.viewmodel.AdminOpState
import com.example.e_commerce_cm.viewmodel.AdminViewModel

private val CATEGORIES = listOf("men's clothing", "women's clothing", "jewelery", "electronics")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductFormScreen(
    adminViewModel: AdminViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val selectedProduct by adminViewModel.selectedProduct.collectAsState()
    val opState by adminViewModel.opState.collectAsState()
    val isEditing = selectedProduct != null
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf(selectedProduct?.title ?: "") }
    var titleTouched by remember { mutableStateOf(false) }

    var price by remember { mutableStateOf(selectedProduct?.price?.toString() ?: "") }
    var priceTouched by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf(selectedProduct?.description ?: "") }
    var descriptionTouched by remember { mutableStateOf(false) }

    var category by remember { mutableStateOf(selectedProduct?.category ?: CATEGORIES.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var image by remember { mutableStateOf(selectedProduct?.image ?: "") }
    var imageTouched by remember { mutableStateOf(false) }

    // Validaciones
    val isTitleValid = title.isNotBlank()
    val priceDouble = price.toDoubleOrNull()
    val isPriceValid = priceDouble != null && priceDouble > 0
    val isDescriptionValid = description.isNotBlank()
    val isImageValid = image.startsWith("http://") || image.startsWith("https://")

    val isFormValid = isTitleValid && isPriceValid && isDescriptionValid && isImageValid

    LaunchedEffect(Unit) {
        adminViewModel.opState.collect { state ->
            when (state) {
                is AdminOpState.Success -> {
                    adminViewModel.resetOpState()
                    onSuccess()
                }
                is AdminOpState.Error -> {
                    snackbarHostState.showSnackbar(state.message)
                    adminViewModel.resetOpState()
                }
                else -> Unit
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        focusedLabelColor = MaterialTheme.colorScheme.secondary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Editar producto" else "Nuevo producto",
                        fontWeight = FontWeight.Bold
                    )
                },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleTouched = true },
                label = { Text("Título") },
                isError = titleTouched && !isTitleValid,
                supportingText = {
                    if (titleTouched && !isTitleValid) Text("El título es obligatorio")
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                singleLine = true
            )

            // Precio — solo acepta dígitos y un punto decimal
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    // Filtrar: solo dígitos y un punto decimal
                    val filtered = input.filter { it.isDigit() || it == '.' }
                    val dotCount = filtered.count { it == '.' }
                    if (dotCount <= 1) {
                        price = filtered
                        priceTouched = true
                    }
                },
                label = { Text("Precio (USD)") },
                isError = priceTouched && !isPriceValid,
                supportingText = {
                    when {
                        priceTouched && price.isBlank() -> Text("El precio es obligatorio")
                        priceTouched && !isPriceValid -> Text("Ingresa un precio válido mayor a 0")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                singleLine = true
            )

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descriptionTouched = true },
                label = { Text("Descripción") },
                isError = descriptionTouched && !isDescriptionValid,
                supportingText = {
                    if (descriptionTouched && !isDescriptionValid) Text("La descripción es obligatoria")
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                minLines = 3,
                maxLines = 5
            )

            // Categoría (dropdown, siempre válido)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { category = cat; categoryExpanded = false }
                        )
                    }
                }
            }

            // URL de imagen
            OutlinedTextField(
                value = image,
                onValueChange = { image = it; imageTouched = true },
                label = { Text("URL de imagen") },
                isError = imageTouched && !isImageValid,
                supportingText = {
                    when {
                        imageTouched && image.isBlank() -> Text("La URL es obligatoria")
                        imageTouched && !isImageValid -> Text("Debe comenzar con http:// o https://")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors,
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        adminViewModel.updateProduct(
                            selectedProduct!!.id, title, priceDouble!!, description, category, image
                        )
                    } else {
                        adminViewModel.createProduct(title, priceDouble!!, description, category, image)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isFormValid && opState !is AdminOpState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (opState is AdminOpState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (isEditing) "Guardar cambios" else "Crear producto",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
