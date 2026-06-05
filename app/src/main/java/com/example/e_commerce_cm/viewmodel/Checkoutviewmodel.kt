package com.example.e_commerce_cm.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeliveryOrder(
    val fullName: String,
    val phone: String,
    val address: String,
    val city: String,
    val department: String,
    val notes: String,
    val total: Double
)

class CheckoutViewModel : ViewModel() {

    private val _lastOrder = MutableStateFlow<DeliveryOrder?>(null)
    val lastOrder: StateFlow<DeliveryOrder?> = _lastOrder.asStateFlow()

    fun placeOrder(
        fullName: String,
        phone: String,
        address: String,
        city: String,
        department: String,
        notes: String,
        total: Double
    ) {
        _lastOrder.value = DeliveryOrder(
            fullName   = fullName,
            phone      = phone,
            address    = address,
            city       = city,
            department = department,
            notes      = notes,
            total      = total
        )
        // Aquí puedes guardar el pedido en Firestore en el futuro:
        // viewModelScope.launch { orderRepository.saveOrder(_lastOrder.value!!) }
    }
}