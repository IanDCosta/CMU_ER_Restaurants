package pt.ipp.estg.cmu_restaurants

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.cmu_restaurants.Firebase.getRestaurantById
import pt.ipp.estg.cmu_restaurants.Firebase.getReviewsByRestaurantIdFromFirestore
import pt.ipp.estg.cmu_restaurants.Firebase.getUserById
import pt.ipp.estg.cmu_restaurants.Models.Restaurant
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.ui.theme.customAccent
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewForm(navController: NavController, restaurantId: String?, context: Context) {
    val colors = TextFieldDefaults.outlinedTextFieldColors(
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
    )

    val db = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    val reviews = remember { mutableStateListOf<Review>() }
    var rating by remember { mutableDoubleStateOf(0.0) }
    var comment by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var pictureUri by remember { mutableStateOf<Uri?>(null) }

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
            pictureUri = tempPhotoUri
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

    LaunchedEffect(restaurantId) {
        coroutineScope.launch {
            getRestaurantById(restaurantId.toString()) { fetchedRestaurant ->
                restaurant = fetchedRestaurant
            }
            val restaurantReviews =
                getReviewsByRestaurantIdFromFirestore(restaurantId.toString())
            reviews.addAll(restaurantReviews)

            val newUser = FirebaseAuth.getInstance().currentUser
            userId = newUser?.uid
                ?: throw IllegalStateException("User not authenticated")

            getUserById(userId) { fetchedUser ->
                name = fetchedUser.name
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.secondary,
        topBar = {
            TopAppBar(
                title = { Text("Write Review", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3C0A3D))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        cameraLauncher.launch(tempPhotoUri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    Text(
                        text = "Take Picture",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                pictureUri?.let {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Captured Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                RatingSelector(
                    rating = rating,
                    onRatingChange = { rating = it }
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment", color = MaterialTheme.colorScheme.customAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Button(
                    onClick = {
                        if (rating.toString().isEmpty() || comment.isEmpty()) {
                            Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }
                        if (rating < 1 || rating > 5) {
                            Toast.makeText(context, "Invalid rating format.", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        val review = Review(
                            userId = userId,
                            restaurantId = restaurantId.toString(),
                            userName = name,
                            restaurantName = restaurant?.restaurantName.toString(),
                            rating = rating,
                            comment = comment,
                            picture = pictureUri.toString()
                        )

                        saveReview(review, db, context) {
                            reviews.add(review)

                            val updatedRestaurant = Restaurant(
                                restaurantId = restaurantId.toString(),
                                restaurantName = restaurant?.restaurantName.toString(),
                                rating = CalculateTotalRating(reviews),
                                address = restaurant?.address.toString(),
                                lat = restaurant?.lat ?: 0.0,
                                lon = restaurant?.lon ?: 0.0
                            )

                            updateRestaurant(
                                restaurantId.toString(),
                                updatedRestaurant
                            ) { success ->
                                if (success) {
                                    Log.d("UpdateRestaurant", "Restaurant updated successfully!")
                                } else {
                                    Log.e("UpdateRestaurant", "Failed to update restaurant.")
                                }
                            }

                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Submit Review", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun RatingSelector(
    rating: Double,
    onRatingChange: (Double) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Rating $i",
                tint = if (i <= rating) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChange(i.toDouble()) }
            )
        }
    }
}

fun saveReview(review: Review, db: FirebaseFirestore, context: Context, onSuccess: () -> Unit) =
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val reviewData = hashMapOf(
                "userId" to review.userId,
                "restaurantId" to review.restaurantId,
                "userName" to review.userName,
                "restaurantName" to review.restaurantName,
                "rating" to review.rating,
                "comment" to review.comment,
                "picture" to review.picture,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("reviews").document(review.reviewId).set(reviewData).await()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Review submitted successfully.", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.println(Log.DEBUG, "Log", e.message.toString());
                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

fun updateRestaurant(
    restaurantId: String,
    updatedRestaurant: Restaurant,
    onResult: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val updatedData = mapOf(
        "restaurantName" to updatedRestaurant.restaurantName,
        "rating" to updatedRestaurant.rating,
        "address" to updatedRestaurant.address,
        "lat" to updatedRestaurant.lat,
        "lon" to updatedRestaurant.lon
    )

    db.collection("restaurants")
        .document(restaurantId)
        .update(updatedData)
        .addOnSuccessListener {
            onResult(true)
        }
        .addOnFailureListener { exception ->
            exception.printStackTrace()
            onResult(false)
        }
}

fun CalculateTotalRating(reviews: List<Review>): Double {
    if (reviews.isEmpty()) {
        return 5.0
    }

    val totalRating = reviews.sumOf { it.rating }
    return BigDecimal(totalRating / reviews.size).setScale(2, RoundingMode.HALF_UP).toDouble()
}