package com.example.e_commerce_cm.data.repository

import com.example.e_commerce_cm.data.local.AppDatabase
import com.example.e_commerce_cm.data.local.ProductEntity
import com.example.e_commerce_cm.data.local.toEntity
import com.example.e_commerce_cm.data.local.toProduct
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.model.Rating
import com.example.e_commerce_cm.data.network.RetrofitInstance
import android.content.Context

class AdminRepository(context: Context) {
    private val api = RetrofitInstance.api
    private val dao = AppDatabase.getInstance(context).productDao()

    // Devuelve productos desde Room; si está vacío, carga desde API primero
    suspend fun getAllProducts(): List<Product> {
        if (dao.count() == 0) {
            val apiProducts = api.getAllProducts()
            dao.insertAll(apiProducts.map { it.toEntity() })
        }
        return dao.getAll().map { it.toProduct() }
    }

    suspend fun createProduct(
        title: String, price: Double, description: String, category: String, image: String
    ): Product {
        val newId = (dao.maxId() ?: 0) + 1
        val entity = ProductEntity(
            id = newId, title = title, price = price,
            description = description, category = category,
            image = image, ratingRate = 0.0, ratingCount = 0
        )
        dao.insert(entity)
        return entity.toProduct()
    }

    suspend fun updateProduct(
        id: Int, title: String, price: Double, description: String, category: String, image: String
    ): Product {
        val entity = ProductEntity(
            id = id, title = title, price = price,
            description = description, category = category,
            image = image, ratingRate = 0.0, ratingCount = 0
        )
        dao.update(entity)
        return entity.toProduct()
    }

    suspend fun deleteProduct(id: Int) {
        dao.deleteById(id)
    }
}
