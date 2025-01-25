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
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import pt.ipp.estg.cmu_restaurants.ui.theme.Cmu_restaurantsTheme

class MainActivity : ComponentActivity(), PermissionsListener {
    lateinit var permissionsManager: PermissionsManager
    private lateinit var powerManager: PowerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Toast.makeText(this, "Activated location", Toast.LENGTH_LONG).show()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }

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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // Provide explanation to the user (e.g., Toast or Dialog)
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
            // Permission denied, handle accordingly
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