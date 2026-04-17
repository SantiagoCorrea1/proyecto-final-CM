package com.example.e_commerce_cm.data.model

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating
)

data class Rating(
    val rate: Double,
    val count: Int
)

data class CartItem(
    val product: Product,
    val quantity: Int
)
