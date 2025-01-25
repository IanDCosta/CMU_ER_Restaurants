package pt.ipp.estg.cmu_restaurants.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "restaurants")
data class Restaurant (
    @PrimaryKey val restaurantId: String = UUID.randomUUID().toString(),
    val restaurantName: String,
    val rating: Double,
    val address: String,
    val lat: Double,
    val lon: Double
){
    constructor() : this("","",0.0,"0",0.0,0.0)
}