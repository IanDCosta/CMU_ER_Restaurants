package pt.ipp.estg.cmu_restaurants.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipp.estg.cmu_restaurants.Firebase.getAllRestaurants
import pt.ipp.estg.cmu_restaurants.R
import pt.ipp.estg.cmu_restaurants.Models.Restaurant
import java.util.Calendar
import java.util.Locale

class RestaurantNotificationService : Service() {

    private lateinit var locationManager: LocationManager
    private val restaurantsList = mutableListOf<Restaurant>()

    override fun onCreate() {
        super.onCreate()
        Log.d("RestaurantService", "Service created")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                fetchNearbyRestaurants(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            60000L,
            50f,
            locationListener,
            Looper.getMainLooper()
        )
    }

    private fun fetchNearbyRestaurants(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fetchedRestaurants = getAllRestaurants()

                withContext(Dispatchers.Main) {
                    restaurantsList.clear()
                    restaurantsList.addAll(fetchedRestaurants)
                    checkProximityToRestaurants(location)
                }
            } catch (e: Exception) {
                Log.e("RestaurantService", "Error fetching restaurants: ${e.message}")
            }
        }
    }

    private fun checkProximityToRestaurants(location: Location) {
        if (!isWithinLunchOrDinnerTime()) {
            Log.d("RestaurantService", "Current time is outside lunch/dinner hours. No notifications will be sent.")
            return
        }

        for (restaurant in restaurantsList) {
            val restaurantLocation = Location("").apply {
                latitude = restaurant.lat
                longitude = restaurant.lon
            }

            val distance = location.distanceTo(restaurantLocation)
            Log.d("ProximityCheck", "Distance to ${restaurant.restaurantName}: $distance meters")

            if (distance <= 50) {
                sendNotification(restaurant)
            }
        }
    }

    private fun isWithinLunchOrDinnerTime(): Boolean {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        //lunch
        if ((currentHour == 12 && currentMinute >= 0) || (currentHour in 13..14) || (currentHour == 15 && currentMinute == 0)) {
            return true
        }

        //dinner
        if ((currentHour == 19 && currentMinute >= 0) || (currentHour == 20) || (currentHour == 21 && currentMinute == 0)) {
            return true
        }

        return false
    }

    private fun sendNotification(restaurant: Restaurant) {
        val channelId = "restaurant_notification_channel"
        createNotificationChannel(channelId, "Restaurant Notifications", NotificationManager.IMPORTANCE_HIGH)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nearby Restaurant Alert")
            .setContentText("I heard ${restaurant.restaurantName} makes delicious Francesinhas!")
            .setSmallIcon(R.drawable.restaurant_icon)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(restaurant.restaurantId.hashCode(), notification)
    }

    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        Log.d("RestaurantService", "Service started")
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "restaurant_service_channel"
        createNotificationChannel(channelId, "Restaurant Service", NotificationManager.IMPORTANCE_LOW)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Restaurant Service Running")
            .setContentText("Tracking nearby restaurants...")
            .setSmallIcon(R.drawable.restaurant_icon)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
