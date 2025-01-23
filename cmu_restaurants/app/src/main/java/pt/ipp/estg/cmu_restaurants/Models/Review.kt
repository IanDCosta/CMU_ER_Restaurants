package pt.ipp.estg.cmu_restaurants.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val reviewId: String,
    val userId: String,
    val stars: Int,
    val review: String
)
