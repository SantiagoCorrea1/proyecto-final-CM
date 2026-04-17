package com.example.e_commerce_cm.viewmodel

import androidx.lifecycle.ViewModel
import com.example.e_commerce_cm.data.model.CartItem
import com.example.e_commerce_cm.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(CartItem(product, 1))
        }
        _cartItems.value = current
    }

    fun removeFromCart(productId: Int) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun increaseQuantity(productId: Int) {
        _cartItems.value = _cartItems.value.map {
            if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
        }
    }

    fun decreaseQuantity(productId: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (current[index].quantity <= 1) current.removeAt(index)
            else current[index] = current[index].copy(quantity = current[index].quantity - 1)
        }
        _cartItems.value = current
    }

    fun getTotal(): Double = _cartItems.value.sumOf { it.product.price * it.quantity }
}
