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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu_restaurants.Firebase.getRestaurantById
import pt.ipp.estg.cmu_restaurants.Firebase.getReviewsByRestaurantNameFromFirestore
import pt.ipp.estg.cmu_restaurants.Models.Restaurant
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.Models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfile(navController: NavController, restaurantId: String?) {
    val reviews = remember { mutableStateListOf<Review>() }
    val coroutineScope = rememberCoroutineScope()
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }

    LaunchedEffect(restaurantId) {
        coroutineScope.launch {
            getRestaurantById(restaurantId.toString()) { fetchedRestaurant ->  restaurant = fetchedRestaurant}
            val restaurantReviews =
                getReviewsByRestaurantNameFromFirestore(restaurantId.toString())
            reviews.addAll(restaurantReviews)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${restaurant?.restaurantName}'s Reviews") },
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
                    navController.navigate("reviewForm/$restaurantId")
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