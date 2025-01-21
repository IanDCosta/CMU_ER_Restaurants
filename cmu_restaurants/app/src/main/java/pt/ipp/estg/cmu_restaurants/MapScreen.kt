import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
    val mapViewportState = rememberMapViewportState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
            ) {
                MapEffect(Unit) { mapView ->

                    mapView.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                    mapViewportState.transitionToFollowPuckState()
                }
            }
        }
    )
}

@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit, onSearchClicked: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(text = "Enter an address") }
        )
        Button(
            onClick = { onSearchClicked(query) },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(text = "Search")
        }
    }
}


@Composable
fun ButtonBar(
    navController: NavController,
    userId: String?,
    mapboxMapState: MutableState<MapboxMap?>
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
                Text(text = "Check Profile")
            }
            Button(onClick = { navController.navigate("tripHistory/$userId") }) {
                Text(text = "Trips")
            }
        }
    }
}

private fun geocodeAddress(context: Context, address: String, onResult: (LatLng) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.mapbox.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geocodingApi = retrofit.create(MapboxGeocodingApi::class.java)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = geocodingApi.geocodeAddress(
                address = address,
                accessToken = "sk.eyJ1IjoiZ2F5YmlydTA4IiwiYSI6ImNtM2VvNTBodDBneXcybnF4cXdrMTVpM3EifQ.Qa2Kk4UxskMS5qj0maENCg"
            )
            if (response.isSuccessful) {
                val feature = response.body()?.features?.firstOrNull()
                feature?.geometry?.coordinates?.let {
//                    val latLng = LatLng(it)
//                    withContext(Dispatchers.Main) {
//                        onResult(latLng)
//                    }
                }
            } else {
                Log.e("Geocode", "Failed to geocode address: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("Geocode", "Error: ${e.message}")
        }
    }
}

//private fun enableLocationComponent(context: Context, style: Style, mapboxMap: MapboxMap) {
//    val locationComponent = mapboxMap.locationComponent
//    locationComponent.activateLocationComponent(
//        LocationComponentActivationOptions.builder(context, style).build()
//    )
//
//    if (ActivityCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED &&
//        ActivityCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//        return
//    }
//
//    locationComponent.isLocationComponentEnabled = true
//    locationComponent.cameraMode = CameraMode.TRACKING
//    locationComponent.renderMode = RenderMode.COMPASS
//}
