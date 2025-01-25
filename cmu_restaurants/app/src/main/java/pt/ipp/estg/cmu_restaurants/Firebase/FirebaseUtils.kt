package pt.ipp.estg.cmu_restaurants.Firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cmu_restaurants.Models.Restaurant
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.Models.User

fun getUserById(userId: String, onUserFetched: (User) -> Unit) {
    val docRef = Firebase.firestore.collection("users").document(userId)
    docRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                document.toObject(User::class.java)?.let { fetchedUser ->
                    onUserFetched(fetchedUser)
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
}

fun getRestaurantById(restaurantId: String, onRestaurantFetched: (Restaurant) -> Unit) {
    val docRef = Firebase.firestore.collection("restaurants").document(restaurantId)
    docRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                document.toObject(Restaurant::class.java)?.let { fetchedUser ->
                    onRestaurantFetched(fetchedUser)
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
}

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

