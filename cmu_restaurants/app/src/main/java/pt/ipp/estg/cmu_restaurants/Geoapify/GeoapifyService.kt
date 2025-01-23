package Models.Geoapify

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoapifyService {
    @GET("v2/places")
    fun getNearbyRestaurants(
        @Query("categories") categories: String = "catering.restaurant",
        @Query("filter") filter: String, // E.g., "circle:lon,lat,radius"
        @Query("limit") limit: Int = 20,
        @Query("apiKey") apiKey: String
    ): Call<GeoapifyPlacesResponse>
}