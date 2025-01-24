package pt.ipp.estg.cmu_restaurants.Models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val reviewId: Int = 0,
    val userId: String,
    val restaurantName: String,
    val rating: Int,
    val comment: String
)
