package pt.ipp.estg.cmu_restaurants.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val profilePicture: String? = "null"
){
    constructor() : this("","","","","",null)
}