import Models.Geoapify.GeoapifyPlacesResponse
import Models.Geoapify.GeoapifyService
import Models.Geoapify.PlaceProperties
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.cmu_restaurants.Models.Restaurant

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pt.ipp.estg.cmu_restaurants.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(navController: NavController, userId: String?) {
    val context = LocalContext.current
    val nearbyRestaurants = remember { mutableStateOf<List<PlaceProperties>>(emptyList()) }
    val mapViewportState = rememberMapViewportState()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.geoapify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val geoapifyService = retrofit.create(GeoapifyService::class.java)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        ButtonBar(navController = navController, userId = userId)

        Row(modifier = Modifier.fillMaxSize()) {
            if (nearbyRestaurants.value.isNotEmpty()) {
                RestaurantSidebar(
                    restaurants = nearbyRestaurants.value,
                    onSelectRestaurant = { restaurant ->
                        findRestaurantByDetails(restaurant, navController)
                    }
                )
            }

            MapboxMap(
                modifier = Modifier
                    .fillMaxWidth(),
                mapViewportState = mapViewportState,
            ) {
                MapEffect(Unit) { mapView ->
                    val annotationPlugin = mapView.annotations
                    val pointAnnotationManager = annotationPlugin.createPointAnnotationManager()

                    mapView.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                    mapViewportState.transitionToFollowPuckState()

                    mapView.getMapboxMap().getStyle { style ->
                        // Load the original marker images
                        val bitmap1 =
                            BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
                        val bitmap2 = BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.restaurant_icon
                        )

                        // Resize both bitmaps to make them smaller
                        val scaledBitmap1 =
                            Bitmap.createScaledBitmap(bitmap1, 50, 50, false)  // Scale to 50x50
                        val scaledBitmap2 =
                            Bitmap.createScaledBitmap(bitmap2, 50, 50, false)  // Scale to 50x50

                        // Add the scaled images to the style
                        style.addImage("red_marker", scaledBitmap1)
                        style.addImage("restaurant_icon", scaledBitmap2)
                    }

                    val onClickListener = object : OnMapClickListener {
                        override fun onMapClick(point: Point): Boolean {
                            pointAnnotationManager.deleteAll()
                            val latitude = point.latitude()
                            val longitude = point.longitude()

                            Log.println(
                                Log.DEBUG,
                                "Log",
                                "latitude: " + latitude + "; longitude: " + longitude
                            )

                            val novoMarker = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage("red_marker")
                            pointAnnotationManager.create(novoMarker)

                            fetchNearbyRestaurants(
                                geoapifyService,
                                longitude,
                                latitude,
                                500,
                                "9a5d8dd32d7c49709e75789d2d28c52d"
                            ) { restaurants ->
                                nearbyRestaurants.value = restaurants

                                nearbyRestaurants.value.forEach { restaurant ->
                                    if (restaurant.name != null) {

                                        val restaurantPoint =
                                            Point.fromLngLat(restaurant.lon, restaurant.lat)

                                        val restaurantMarker = PointAnnotationOptions()
                                            .withPoint(restaurantPoint)
                                            .withIconImage("restaurant_icon")

                                        pointAnnotationManager.create(restaurantMarker)
                                    }
                                }
                            }

                            return true
                        }
                    }
                    mapView.gestures.addOnMapClickListener(onClickListener)
                }

            }
        }
    }

}

fun fetchNearbyRestaurants(
    geoapifyService: GeoapifyService,
    lon: Double,
    lat: Double,
    radius: Int,
    apiKey: String,
    onResult: (List<PlaceProperties>) -> Unit
) {
    val filter = "circle:$lon,$lat,$radius"
    val call = geoapifyService.getNearbyRestaurants(filter = filter, apiKey = apiKey)

    call.enqueue(object : retrofit2.Callback<GeoapifyPlacesResponse> {
        override fun onResponse(
            call: Call<GeoapifyPlacesResponse>,
            response: retrofit2.Response<GeoapifyPlacesResponse>
        ) {
            if (response.isSuccessful) {
                val restaurants = response.body()?.features?.map { it.properties } ?: emptyList()
                restaurants.forEach {
                    Log.d("Log", "Name: ${it.name}, Lat/Lng: ${it.lat}/${it.lon}")
                }
                val rawResponse = response.body()
                Log.d("Log", "Raw Response" + rawResponse.toString())
                onResult(restaurants)
            } else {
                Log.e("Log", response.errorBody()?.string() ?: "Unknown error")
                println("Error: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<GeoapifyPlacesResponse>, t: Throwable) {
            println("Failure: ${t.message}")
        }
    })
}

fun findRestaurantByDetails(
    place: PlaceProperties,
    navController: NavController
) = CoroutineScope(Dispatchers.IO).launch {
    val db = FirebaseFirestore.getInstance()

    try {
        val querySnapshot = db.collection("restaurants")
            .whereEqualTo("address", place.formatted)
            .whereEqualTo("lat", place.lat)
            .whereEqualTo("lon", place.lon)
            .get()
            .await()

        val restaurant = if (!querySnapshot.isEmpty) {
            querySnapshot.documents.first().toObject(Restaurant::class.java)!!
        } else {
            val newRestaurant = Restaurant(
                restaurantName = place.name.toString(),
                rating = 5.0,
                address = place.formatted.toString(),
                lat = place.lat,
                lon = place.lon,
            )
            saveRestaurant(newRestaurant, db)
            newRestaurant
        }

        withContext(Dispatchers.Main) {
            Log.d("Log", "Selected restaurant: $restaurant")
            navController.navigate("restaurantProfile/${restaurant.restaurantId}")
        }
    } catch (exception: Exception) {
        withContext(Dispatchers.Main) {
            Log.e("Log", "Error finding restaurant: ${exception.message}")
        }
    }
}

fun saveRestaurant(restaurant: Restaurant, db: FirebaseFirestore) =
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val restaurantData = hashMapOf(
                "restaurantId" to restaurant.restaurantId,
                "restaurantName" to restaurant.restaurantName,
                "rating" to restaurant.rating,
                "address" to restaurant.address,
                "lat" to restaurant.lat,
                "lon" to restaurant.lon
            )

            db.collection("restaurants").document(restaurant.restaurantId).set(restaurantData)
                .await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.println(Log.DEBUG, "Log", e.message.toString());
            }
        }
    }

@Composable
fun ButtonBar(
    navController: NavController,
    userId: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("userProfile/$userId") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "Profile")
            }
            Button(
                onClick = { navController.navigate("reviews/$userId") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "Reviews")
            }
            Button(
                onClick = { navController.navigate("leaderBoard") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "LeaderBoard")
            }
        }
    }
}

@Composable
fun RestaurantSidebar(
    restaurants: List<PlaceProperties>,
    onSelectRestaurant: (PlaceProperties) -> Unit
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp)
    ) {
        Text(
            "Restaurants",
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        restaurants.forEach { restaurant ->
            val restaurantName = restaurant.name
            if (restaurantName != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectRestaurant(restaurant) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${restaurant.formatted}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }
        }
    }
}