package com.example.e_commerce_cm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.model.Rating

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val ratingRate: Double,
    val ratingCount: Int
)

fun ProductEntity.toProduct() = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    image = image,
    rating = Rating(ratingRate, ratingCount)
)

fun Product.toEntity() = ProductEntity(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    image = image,
    ratingRate = rating.rate,
    ratingCount = rating.count
)
