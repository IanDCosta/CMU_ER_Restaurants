package pt.ipp.estg.cmu_restaurants.Models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
    @PrimaryKey val reviewId: String = UUID.randomUUID().toString(),
    val userId: String,
    val restaurantId: String,
    val userName: String,
    val restaurantName: String,
    val rating: Double,
    val comment: String,
    val picture: String? = "null"
){
    constructor() : this("","","","","",0.0,"",null)
}
