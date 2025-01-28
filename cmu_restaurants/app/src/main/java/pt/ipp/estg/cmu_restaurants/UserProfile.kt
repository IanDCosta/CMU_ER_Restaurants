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
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import pt.ipp.estg.cmu_restaurants.Firebase.AuthViewModel
import pt.ipp.estg.cmu_restaurants.Firebase.getUserById
import pt.ipp.estg.cmu_restaurants.Models.User
import java.io.File
import java.io.FileOutputStream

@Composable
fun UserProfile(userId: String?) {
    val context = LocalContext.current
    val authViewModel = viewModel(AuthViewModel::class.java)
    var language by remember { mutableStateOf("en") }
    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val numericRegex = Regex("[^0-9]")
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    val maxLength = 50

    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File.createTempFile("profile_picture", ".jpg", context.cacheDir)
        )
    }

    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profilePictureUri = tempPhotoUri
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    val nameLabel = if (language == "pt") "Nome" else "Name"
    val phoneLabel = if (language == "pt") "Número de Telefone" else "Phone Number"
    val updateInfoText = if (language == "pt") "Atualizar Informações" else "Update Information"
    val userNotFoundText = if (language == "pt") "Usuário não encontrado" else "User not found"
    val updateSuccessText =
        if (language == "pt") "Atualização bem-sucedida" else "Update Successful"
    val updateFailedText = if (language == "pt") "Atualização falhou" else "Update Failed"

    LaunchedEffect(userId) {
        if (userId != null) {
            getUserById(userId) { fetchedUser ->
                Log.println(Log.DEBUG, "Log", fetchedUser.toString())
                user = fetchedUser
                name = fetchedUser.name
                email = fetchedUser.email
                phoneNumber = fetchedUser.phoneNumber.toString()
                profilePictureUri = fetchedUser.profilePicture?.let { Uri.parse(it) }
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
            Text(
                text = "Email: $email",
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
                    onValueChange = {
                        if (it.length <= maxLength) {
                            name = it.replace("\n", "").replace("\r", "")
                        }
                    },
                    label = { Text(nameLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        val stripped =
                            numericRegex.replace(it.replace("\n", "").replace("\r", ""), "")
                        phoneNumber = if (stripped.length >= 10) {
                            stripped.substring(0..9)
                        } else {
                            stripped
                        }
                    },
                    label = { Text(phoneLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (name.isEmpty() || phoneNumber.isEmpty()) {
                        Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    if (!phoneNumber.isDigitsOnly() && phoneNumber.length > 9) {
                        Toast.makeText(
                            context,
                            "Phone Number must only contain digits",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@Button
                    }

                    val updatedUser = user?.copy(
                        name = name,
                        email = email,
                        phoneNumber = phoneNumber
                    )

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

                        authViewModel.updateUser(userId!!, updatedUser!!) { success ->
                            if (success) {
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
