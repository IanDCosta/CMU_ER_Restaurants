package pt.ipp.estg.cmu_restaurants.Room.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.ipp.estg.cmu_restaurants.Models.Review

@Dao
interface ReviewDao {
    @Insert
    suspend fun insert(review: Review)

    @Query("SELECT * FROM reviews WHERE userId = :userId")
    suspend fun getReviewsByUserId(userId: String?): List<Review>
}