package pt.ipp.estg.cmu_restaurants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.ipp.estg.cmu_restaurants.Firebase.AuthViewModel
import pt.ipp.estg.cmu_restaurants.Models.User
import java.io.File
import java.io.FileOutputStream

@Composable
fun UserProfile(userId: String?) {
    val context = LocalContext.current
    val authViewModel = viewModel(AuthViewModel::class.java)
    var language by remember { mutableStateOf("en") }
    var firestore = Firebase.firestore
    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File.createTempFile("profile_picture", ".jpg", context.cacheDir)
        )
    }

    // Launcher para capturar foto com a câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profilePictureUri = tempPhotoUri
        }
    }

    val nameLabel = if (language == "pt") "Nome" else "Name"
    val emailLabel = if (language == "pt") "Email" else "Email"
    val phoneLabel = if (language == "pt") "Número de Telefone" else "Phone Number"
    val updateInfoText = if (language == "pt") "Atualizar Informações" else "Update Information"
    val userNotFoundText = if (language == "pt") "Usuário não encontrado" else "User not found"
    val updateSuccessText =
        if (language == "pt") "Atualização bem-sucedida" else "Update Successful"
    val updateFailedText = if (language == "pt") "Atualização falhou" else "Update Failed"

    LaunchedEffect(userId) {
        if (userId != null) {
            val docRef = firestore.collection("users").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        document.toObject(User::class.java)?.let { fetchedUser ->
                            user = fetchedUser
                            name = fetchedUser.name ?: ""
                            email = fetchedUser.email ?: ""
                            phoneNumber = fetchedUser.phoneNumber?.toString() ?: ""
                            profilePictureUri = fetchedUser.profilePicture?.let { Uri.parse(it) }
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userNotFoundText,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                language = if (language == "en") "pt" else "en"
            }) {
                Text(text = if (language == "en") "Switch to Portuguese" else "Mudar para Inglês")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable {
                        cameraLauncher.launch(tempPhotoUri)
                    },
                contentAlignment = Alignment.Center
            ) {
                profilePictureUri?.let {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(nameLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(emailLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(phoneLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val updatedUser = phoneNumber.toIntOrNull()?.let {
                        user?.copy(
                            name = name,
                            email = email,
                            phoneNumber = it
                        )
                    }

                    profilePictureUri?.let {
                        try {
                            val profilePictureFile =
                                File(context.filesDir, "profile_picture_${userId}.jpg")
                            context.contentResolver.openInputStream(it)?.use { inputStream ->
                                FileOutputStream(profilePictureFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }

                            updatedUser?.let {
                                val userWithPhoto =
                                    it.copy(profilePicture = profilePictureFile.absolutePath)
                                authViewModel.updateUser(userId!!, userWithPhoto) { success ->
                                    if (success) {
                                        showNotification(context, updateSuccessText)
                                        Toast.makeText(
                                            context,
                                            updateSuccessText,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            updateFailedText,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, updateFailedText, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Error saving profile picture: ", e)
                        }
                    } ?: updatedUser?.let {
                        authViewModel.updateUser(userId!!, it) { success ->
                            if (success) {
                                showNotification(context, updateSuccessText)
                                Toast.makeText(context, updateSuccessText, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(context, updateFailedText, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                Text(
                    text = updateInfoText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "XPTO©",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

fun showNotification(context: Context, message: String) {
    val channelId = "profile_update_channel"
    val channelName = "Profile Update Notifications"
    val notificationId = 1

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for profile updates"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Profile Update")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notify(notificationId, notification)
    }
}

