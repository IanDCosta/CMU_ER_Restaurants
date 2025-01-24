package pt.ipp.estg.cmu_restaurants

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu_restaurants.Firebase.getReviewsByRestaurantNameFromFirestore
import pt.ipp.estg.cmu_restaurants.Models.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfile(navController: NavController, restaurantName: String?) {
    val reviews = remember { mutableStateListOf<Review>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(restaurantName) {
        coroutineScope.launch {
            val restaurantReviews =
                getReviewsByRestaurantNameFromFirestore(restaurantName.toString())
            reviews.addAll(restaurantReviews)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${restaurantName}'s Reviews") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (reviews.isEmpty()) {
                Text("No reviews found.")
            } else {
                LazyColumn {
                    items(reviews) { review ->
                        Text(
                            "Review ID: ${review.reviewId}\n" +
                                    "Rating: ${review.rating}/5\n" +
                                    "Comment: ${review.comment}"
                        )
                    }
                }
            }
            Button(
                onClick = {
                    navController.navigate("reviewForm/$restaurantName")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "Write Review", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}