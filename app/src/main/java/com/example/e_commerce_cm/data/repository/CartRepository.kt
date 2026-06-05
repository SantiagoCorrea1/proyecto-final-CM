package com.example.e_commerce_cm.data.repository

import com.example.e_commerce_cm.data.model.CartItem
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.model.Rating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Gestiona el carrito en Firestore.
 *
 * Estructura:
 *   carts/{uid}/items/{productId}  →  { product: {...}, quantity: N }
 */
class CartRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val fireAuth  = FirebaseAuth.getInstance()

    private val uid get() = fireAuth.currentUser?.uid

    private fun itemsRef(userId: String) =
        firestore.collection("carts").document(userId).collection("items")

    // ── Observar carrito en tiempo real ──────────────────────────────────────
    fun observeCart(): Flow<List<CartItem>> {
        val userId = uid ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = itemsRef(userId).addSnapshotListener { snapshot, _ ->
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val productMap = doc.get("product") as? Map<*, *> ?: return@mapNotNull null
                        val ratingMap  = productMap["rating"] as? Map<*, *>
                        val product = Product(
                            id          = (productMap["id"] as? Number)?.toInt() ?: 0,
                            title       = productMap["title"] as? String ?: "",
                            price       = (productMap["price"] as? Number)?.toDouble() ?: 0.0,
                            description = productMap["description"] as? String ?: "",
                            category    = productMap["category"] as? String ?: "",
                            image       = productMap["image"] as? String ?: "",
                            rating      = Rating(
                                rate  = (ratingMap?.get("rate") as? Number)?.toDouble() ?: 0.0,
                                count = (ratingMap?.get("count") as? Number)?.toInt() ?: 0
                            )
                        )
                        CartItem(
                            product  = product,
                            quantity = (doc.getLong("quantity") ?: 1).toInt()
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(items)
            }
            awaitClose { listener.remove() }
        }
    }

    // ── Agregar / incrementar ítem ────────────────────────────────────────────
    suspend fun addItem(product: Product) {
        val userId = uid ?: return
        val ref = itemsRef(userId).document(product.id.toString())
        val existing = ref.get().await()
        if (existing.exists()) {
            val currentQty = existing.getLong("quantity")?.toInt() ?: 1
            ref.update("quantity", currentQty + 1).await()
        } else {
            val productMap = mapOf(
                "id"          to product.id,
                "title"       to product.title,
                "price"       to product.price,
                "description" to product.description,
                "category"    to product.category,
                "image"       to product.image,
                "rating"      to mapOf(
                    "rate"  to product.rating.rate,
                    "count" to product.rating.count
                )
            )
            ref.set(mapOf("product" to productMap, "quantity" to 1)).await()
        }
    }

    // ── Aumentar cantidad ─────────────────────────────────────────────────────
    suspend fun increase(productId: Int) {
        val userId = uid ?: return
        val ref = itemsRef(userId).document(productId.toString())
        val qty = ref.get().await().getLong("quantity")?.toInt() ?: 1
        ref.update("quantity", qty + 1).await()
    }

    // ── Disminuir o eliminar ──────────────────────────────────────────────────
    suspend fun decrease(productId: Int) {
        val userId = uid ?: return
        val ref = itemsRef(userId).document(productId.toString())
        val qty = ref.get().await().getLong("quantity")?.toInt() ?: 1
        if (qty <= 1) ref.delete().await()
        else ref.update("quantity", qty - 1).await()
    }

    // ── Eliminar ítem completo ────────────────────────────────────────────────
    suspend fun remove(productId: Int) {
        val userId = uid ?: return
        itemsRef(userId).document(productId.toString()).delete().await()
    }

    // ── Vaciar carrito ────────────────────────────────────────────────────────
    suspend fun clear() {
        val userId = uid ?: return
        val batch = firestore.batch()
        itemsRef(userId).get().await().forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}
