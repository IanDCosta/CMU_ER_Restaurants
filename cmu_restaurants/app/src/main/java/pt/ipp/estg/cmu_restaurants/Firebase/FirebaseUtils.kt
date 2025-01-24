package pt.ipp.estg.cmu_restaurants.Firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu_restaurants.Models.Review

suspend fun getReviewsByUserIdFromFirestore(userId: String?): List<Review> {
    val db = FirebaseFirestore.getInstance()
    val reviewsCollection = db.collection("reviews")
    return try {
        val snapshot = reviewsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun getReviewsByRestaurantNameFromFirestore(restaurantName: String): List<Review> {
    val db = FirebaseFirestore.getInstance()
    val reviewsCollection = db.collection("reviews")
    return try {
        val snapshot = reviewsCollection
            .whereEqualTo("restaurantName", restaurantName)
            .get()
            .await()
        snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}