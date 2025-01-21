import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import pt.ipp.estg.cmu_restaurants.R

class PowerSaveForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "power_save_service_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PowerSaveService", "Service criado")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PowerSaveService", "Service iniciado")

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        monitorPowerSaveMode()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PowerSaveService", "Service encerrado")
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun monitorPowerSaveMode() {
        val powerManager = getSystemService(POWER_SERVICE) as android.os.PowerManager
        if (powerManager.isPowerSaveMode) {
            Log.d("PowerSaveService", "Modo de economia de energia está ativado")
        } else {
            Log.d("PowerSaveService", "Modo de economia de energia está desativado")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Power Save Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitorando Economia de Energia")
            .setContentText("O modo de economia de energia está a ser monitorizado.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

}
