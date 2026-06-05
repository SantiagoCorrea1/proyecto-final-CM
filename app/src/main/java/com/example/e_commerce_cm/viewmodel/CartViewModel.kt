package com.example.e_commerce_cm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_cm.data.model.CartItem
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val repository = CartRepository()
    private val fireAuth   = FirebaseAuth.getInstance()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        // Observar carrito en tiempo real si hay sesión activa
        if (fireAuth.currentUser != null) observeCart()
    }

    // Llama esto después de hacer login para activar la escucha
    fun observeCart() {
        viewModelScope.launch {
            repository.observeCart().collect { items ->
                _cartItems.value = items
            }
        }
    }

    fun addToCart(product: Product) {
        if (fireAuth.currentUser != null) {
            // Usuario autenticado → guardar en Firestore
            viewModelScope.launch {
                repository.addItem(product)
                // La lista se actualiza automáticamente por observeCart()
            }
        } else {
            // Sin sesión → solo en memoria
            val current = _cartItems.value.toMutableList()
            val index = current.indexOfFirst { it.product.id == product.id }
            if (index >= 0) current[index] = current[index].copy(quantity = current[index].quantity + 1)
            else current.add(CartItem(product, 1))
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Int) {
        if (fireAuth.currentUser != null) {
            viewModelScope.launch { repository.remove(productId) }
        } else {
            _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        }
    }

    fun increaseQuantity(productId: Int) {
        if (fireAuth.currentUser != null) {
            viewModelScope.launch { repository.increase(productId) }
        } else {
            _cartItems.value = _cartItems.value.map {
                if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    fun decreaseQuantity(productId: Int) {
        if (fireAuth.currentUser != null) {
            viewModelScope.launch { repository.decrease(productId) }
        } else {
            val current = _cartItems.value.toMutableList()
            val index = current.indexOfFirst { it.product.id == productId }
            if (index >= 0) {
                if (current[index].quantity <= 1) current.removeAt(index)
                else current[index] = current[index].copy(quantity = current[index].quantity - 1)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        if (fireAuth.currentUser != null) {
            viewModelScope.launch { repository.clear() }
        } else {
            _cartItems.value = emptyList()
        }
    }

    fun getTotal(): Double = _cartItems.value.sumOf { it.product.price * it.quantity }
}
