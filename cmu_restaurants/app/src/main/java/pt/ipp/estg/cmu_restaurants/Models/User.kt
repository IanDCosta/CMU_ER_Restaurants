package pt.ipp.estg.cmu_restaurants.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: Int = 0,
    val password: String = "",
    val profilePicture: String? = "null"
)