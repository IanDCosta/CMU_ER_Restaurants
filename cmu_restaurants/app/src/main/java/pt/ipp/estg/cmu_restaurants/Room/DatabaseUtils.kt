package pt.ipp.estg.cmu_restaurants.Room

import android.content.Context
import kotlinx.coroutines.runBlocking
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.Models.User

//user stuff
fun insertUser(context: Context, user: User) {
    val db = DatabaseProvider.getDatabase(context)
    runBlocking {
        db.userDao().insert(user)
    }
}

fun getUserByEmail(context: Context, email: String): User? {
    val db = DatabaseProvider.getDatabase(context)
    return runBlocking {
        db.userDao().getUserByEmail(email)
    }
}

fun getUserByPhoneNumber(context: Context, phoneNumber: Int): User? {
    val db = DatabaseProvider.getDatabase(context)
    return db.userDao().getUserByPhoneNumber(phoneNumber)
}

fun getUserById(context: Context, id: String): User? {
    val db = DatabaseProvider.getDatabase(context)
    return runBlocking {
        db.userDao().getUserById(id)
    }
}

fun validateUser(context: Context, email: String, password: String): Boolean {
    val db = DatabaseProvider.getDatabase(context)
    return runBlocking {
        val user = db.userDao().getUserByEmail(email)
        user?.password == password
    }
}

suspend fun clearUserTable(context: Context) {
    val db = DatabaseProvider.getDatabase(context)
    return runBlocking {
        db.userDao().clearTable()
    }
}

//review stuff
suspend fun insertReview(context: Context, review: Review) {
    val db = DatabaseProvider.getDatabase(context)
    runBlocking {
        db.reviewDao().insert(review)
    }
}

suspend fun getReviewsByUserId(context: Context, userId: String?): List<Review> {
    val db = DatabaseProvider.getDatabase(context)
    return runBlocking {
        db.reviewDao().getReviewsByUserId(userId)
    }
}