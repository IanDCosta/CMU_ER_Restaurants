import Models.Geoapify.GeoapifyPlacesResponse
import Models.Geoapify.GeoapifyService
import Models.Geoapify.PlaceProperties
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.type.LatLng
import com.mapbox.geojson.Point
import com.mapbox.geojson.LineString
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.Plugin
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
import kotlinx.coroutines.withContext
import pt.ipp.estg.cmu_restaurants.Models.MapboxGeocodingApi
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
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

    Column(modifier = Modifier.fillMaxSize()) {
        ButtonBar(navController = navController, userId = userId)

        Row(modifier = Modifier.fillMaxSize()) {
            if (nearbyRestaurants.value.isNotEmpty()) {
                RestaurantSidebar(
                    restaurants = nearbyRestaurants.value,
                    onSelectRestaurant = { restaurant ->
                        println("Selected restaurant: ${restaurant.name}")
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
                        val bitmap =
                            BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
                        style.addImage("red_marker", bitmap)
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
                            );
                            Log.println(
                                Log.DEBUG,
                                "Log",
                                "restaurants: " + nearbyRestaurants.value
                            );

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
            Button(onClick = { navController.navigate("userProfile/$userId") }) {
                Text(text = "Profile")
            }
            Button(onClick = { navController.navigate("reviews/$userId") }) {
                Text(text = "Your Reviews")
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
            .background(Color.Magenta)
            .padding(8.dp)
    ) {
        Text("Nearby Restaurants", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        restaurants.forEach { restaurant ->
            val restaurantName = restaurant.name ?: "Unknown Restaurant"
            Text(
                text = restaurantName,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRestaurant(restaurant) }
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}