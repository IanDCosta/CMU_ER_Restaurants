package pt.ipp.estg.cmu_restaurants

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu_restaurants.Firebase.getReviewsByUserIdFromFirestore
import pt.ipp.estg.cmu_restaurants.Models.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviews(navController: NavController, userId: String?) {
    val context = LocalContext.current
    val reviews = remember { mutableStateListOf<Review>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val userReviews = getReviewsByUserIdFromFirestore(userId)
            Log.println(Log.DEBUG, "Log", userReviews.toString())
            reviews.addAll(userReviews)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Reviews") },
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(vertical = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            review.picture?.let {
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
                            Text(
                                text = "Restaurant: ${review.restaurantName}\n" +
                                        "Rating: ${(review.rating).toInt()}/5\n" +
                                        "Comment: ${review.comment}",
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}