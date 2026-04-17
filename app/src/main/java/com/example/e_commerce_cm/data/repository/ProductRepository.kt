package com.example.e_commerce_cm.data.repository

import android.content.Context
import com.example.e_commerce_cm.data.local.AppDatabase
import com.example.e_commerce_cm.data.local.toEntity
import com.example.e_commerce_cm.data.local.toProduct
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.network.RetrofitInstance

class ProductRepository(context: Context) {
    private val api = RetrofitInstance.api
    private val dao = AppDatabase.getInstance(context).productDao()

    private suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            val products = api.getAllProducts()
            dao.insertAll(products.map { it.toEntity() })
        }
    }

    suspend fun getMensClothing(): List<Product> {
        seedIfEmpty()
        return dao.getByCategory("men's clothing").map { it.toProduct() }
    }

    suspend fun getWomensClothing(): List<Product> {
        seedIfEmpty()
        return dao.getByCategory("women's clothing").map { it.toProduct() }
    }

    suspend fun getAllClothing(): List<Product> {
        seedIfEmpty()
        return dao.getAll().map { it.toProduct() }
    }

    suspend fun getProductById(id: Int): Product {
        seedIfEmpty()
        return dao.getAll().first { it.id == id }.toProduct()
    }
}
