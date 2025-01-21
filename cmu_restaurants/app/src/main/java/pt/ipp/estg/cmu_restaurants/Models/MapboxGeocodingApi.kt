package pt.ipp.estg.cmu_restaurants.Models

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapboxGeocodingApi {
    @GET("geocoding/v5/mapbox.places/{address}.json")
    suspend fun geocodeAddress(
        @Path("address") address: String,
        @Query("access_token") accessToken: String
    ): Response<GeocodingResponse>
}
