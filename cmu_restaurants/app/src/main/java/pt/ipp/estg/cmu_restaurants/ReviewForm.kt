package pt.ipp.estg.cmu_restaurants

import Models.Geoapify.PlaceProperties
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.ui.theme.customAccent
import pt.ipp.estg.cmu_restaurants.ui.theme.customBackground


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewForm(restaurantName: String?, context: Context) {
    val colors = TextFieldDefaults.outlinedTextFieldColors(
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
    )

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    var ratingError by remember { mutableStateOf(false) }

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
                OutlinedTextField(
                    value = rating.toString(),
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            rating = it.toInt()
                            ratingError = false
                        } else {
                            ratingError = true
                        }
                    },
                    label = {
                        Text(
                            "Rating (1 to 5)",
                            color = MaterialTheme.colorScheme.customAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
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

                        val db = Firebase.firestore

                        val newUser = FirebaseAuth.getInstance().currentUser
                        val uid = newUser?.uid
                            ?: throw IllegalStateException("User not authenticated")

                        val review = Review(
                            userId = uid,
                            restaurantName = restaurantName.toString(),
                            rating = rating,
                            comment = comment
                        )

                        saveReview(review, db, context)
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

fun saveReview(review: Review, db: FirebaseFirestore, context: Context) =
    CoroutineScope(Dispatchers.IO).launch {
    try{
        val reviewData = hashMapOf(
            "reviewId" to review.reviewId,
            "userId" to review.userId,
            "restaurantName" to review.restaurantName,
            "rating" to review.rating,
            "comment" to review.comment
        )

        db.collection("reviews").document().set(reviewData).await()

    }catch(e: Exception){
        withContext(Dispatchers.Main) {
            Log.println(Log.DEBUG, "Log", e.message.toString());
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }
}