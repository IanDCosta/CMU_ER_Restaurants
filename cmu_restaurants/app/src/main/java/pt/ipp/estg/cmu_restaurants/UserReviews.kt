package pt.ipp.estg.cmu_restaurants

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.Room.getReviewsByUserId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviews(navController: NavController, userId: String?){
    val context = LocalContext.current
    val reviews = remember { mutableStateListOf<Review>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val userReviews = getReviewsByUserId(context, userId)
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
                    items(reviews){review ->
                        Text("Review ID: ${review.reviewId}\n" +
                                "Stars: ${review.stars}/5\n" +
                                "Review: ${review.review}")
                    }
                }
            }
        }
    }
}