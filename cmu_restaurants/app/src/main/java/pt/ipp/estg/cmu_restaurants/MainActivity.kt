package pt.ipp.estg.cmu_restaurants

import MainScreen
import PowerSaveForegroundService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import pt.ipp.estg.cmu_restaurants.services.RestaurantNotificationService
import pt.ipp.estg.cmu_restaurants.ui.theme.Cmu_restaurantsTheme

class MainActivity : ComponentActivity(), PermissionsListener {
    lateinit var permissionsManager: PermissionsManager
    private lateinit var powerManager: PowerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        permissionsManager = PermissionsManager(this)

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d("MainActivity", "Permissions already granted")
            Toast.makeText(this, "Activated location", Toast.LENGTH_LONG).show()
        } else {
            Log.d("MainActivity", "Requesting location permissions")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }

        val serviceIntent = Intent(this, RestaurantNotificationService::class.java)
        startForegroundService(serviceIntent)

        if (powerManager.isPowerSaveMode) {
            applyPowerSaveModeAdjustments()
            showPowerSaveModeNotification(this)
            startPowerSaveForegroundService(this)
        } else {
            restoreNormalModeAdjustments()
            stopPowerSaveForegroundService(this)
        }

        setContent {
            Cmu_restaurantsTheme {
                MainScreen()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(
            this,
            "Location permission is needed to show your location.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            Toast.makeText(this, "Activated location", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_LONG).show()
        }
    }
}

private fun applyPowerSaveModeAdjustments() {
    val db = Firebase.firestore
    val newSettings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    db.firestoreSettings = newSettings
    Log.d("MainActivity", "Persistência offline ligada no modo de economia de bateria")
}

private fun restoreNormalModeAdjustments() {
    Log.d("MainActivity", "Modo normal ativado")
}

private fun showPowerSaveModeNotification(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = 1
    val channelId = "power_save_mode_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Power Save Mode",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificação para modo de baixo consumo"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Modo de Baixo Consumo Ativado")
        .setContentText("O modo de baixo consumo está ativado.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    notificationManager.notify(notificationId, notification)
}

private fun startPowerSaveForegroundService(context: Context) {
    val intent = Intent(context, PowerSaveForegroundService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

private fun stopPowerSaveForegroundService(context: Context) {
    val intent = Intent(context, PowerSaveForegroundService::class.java)
    context.stopService(intent)
}