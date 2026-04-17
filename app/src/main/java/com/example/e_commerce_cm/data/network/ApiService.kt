package com.example.e_commerce_cm.data.network

import com.example.e_commerce_cm.data.model.LoginRequest
import com.example.e_commerce_cm.data.model.LoginResponse
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.model.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("products/category/men's clothing")
    suspend fun getMensClothing(): List<Product>

    @GET("products/category/women's clothing")
    suspend fun getWomensClothing(): List<Product>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Product

    @GET("products")
    suspend fun getAllProducts(): List<Product>

    @POST("products")
    suspend fun createProduct(@Body product: Product): Product

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Product

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Product

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("users")
    suspend fun createUser(@Body user: User): User

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): User

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): User

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): User
}
