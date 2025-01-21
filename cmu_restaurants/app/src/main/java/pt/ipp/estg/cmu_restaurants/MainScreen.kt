import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.clickable
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ipp.estg.cmu_restaurants.Models.AuthViewModel
import pt.ipp.estg.cmu_restaurants.UserForm
import pt.ipp.estg.cmu_restaurants.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("userForm") { UserForm(navController, context) }
        composable("map/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            MapScreen(navController = navController, userId = userId)
        }
        composable("userProfile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserProfile(userId = userId, onUpdateClick = {})
        }
//        composable("tripHistory/{userId}") { backStackEntry ->
//            val userIdString = backStackEntry.arguments?.getString("userId")
//            val userId = userIdString?.toIntOrNull() ?: -1
//            val tripDao = DatabaseProvider.getDatabase(context).tripDao()
//            TripHistoryScreen(navController = navController, userId = userId, tripDao = tripDao)
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val backgroundColor = Color(0xFF3C0A3D)
    val accentColor = Color(0xFFE94D9B)
    val textColor = Color.White

    val emailText = rememberSaveable { mutableStateOf("") }
    val passwordText = rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }

    val authViewModel: AuthViewModel = viewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentColor = textColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Login",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = emailText.value,
                onValueChange = { emailText.value = it },
                label = { Text("Your Email", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordText.value,
                onValueChange = { passwordText.value = it },
                label = { Text("Your Password", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Gray,
                ),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val email = emailText.value
                    val password = passwordText.value

                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(
                            context,
                            "Email and password cannot be empty.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    authViewModel.login(email, password) { success, userId ->
                        if (success) {
                                navController.navigate("map/$userId")
                                showNotification(context, "Login Successful")
                        } else {
                            Toast.makeText(
                                context,
                                "Login Failed. Check credentials.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
//                    val isValidUser = validateUser(context, emailText.value, passwordText.value)
//                    if (isValidUser) {
//                        val user = getUserByEmail(context, emailText.value)
//                        if (user != null) {
//                            val userId = user.userId
//                            Log.d("LoginScreen", "Retrieved userId: $userId")
//                            navController.navigate("map/$userId")
//                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
//                        } else {
//                            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        val isValidDriver =
//                            validateDriver(context, emailText.value, passwordText.value)
//                        if (isValidDriver) {
//                            val driver = getDriverByEmail(context, emailText.value)
//                            if (driver != null) {
//                                val driverId = driver.driverId
//                                Log.d("LoginScreen", "Retrieved driverId: $driverId")
//                                navController.navigate("map/$driverId")
//                                Toast.makeText(
//                                    context,
//                                    "Driver Login Successful",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            } else {
//                                Toast.makeText(context, "Driver not found.", Toast.LENGTH_SHORT)
//                                    .show()
//                            }
//                        } else {
//                            // Neither User nor Driver matched
//                            Toast.makeText(
//                                context,
//                                "Invalid email or password.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(text = "Log In", color = textColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "I Forgot my Password",
                color = accentColor,
                fontSize = 14.sp,
                modifier = Modifier.clickable { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Don't have an account?",
                color = textColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Register",
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("userForm") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "XPTO©",
                color = accentColor,
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val backgroundColor = Color(0xFF3C0A3D)
    val accentColor = Color(0xFFE94D9B)
    val textColor = Color.White

    val emailText = rememberSaveable { mutableStateOf("") }
    val passwordText = rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentColor = textColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Register Here",
                color = accentColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = emailText.value,
                onValueChange = { emailText.value = it },
                label = { Text("Your Email", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Gray,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordText.value,
                onValueChange = { passwordText.value = it },
                label = { Text("Your Password", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    focusedLabelColor = accentColor,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color.Gray,
                ),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val email = emailText.value
                    val password = passwordText.value

                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(
                            context,
                            "Email and password cannot be empty.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    authViewModel.register(email, password) { success ->
                        if (success) {
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT)
                                .show()
                            navController.navigate("login")
                        } else {
                            Toast.makeText(
                                context,
                                "Registration Failed. Try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(text = "Register", color = textColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already have an account?",
                color = textColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log In",
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("login") }
            )
        }
    }
}

fun showNotification(context: Context, message: String) {
    val channelId = "login_channel"
    val channelName = "Login Notifications"
    val notificationId = 1

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for logins"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Criar a notificação
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.checkbox_on_background)
        .setContentTitle("Login")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    // Exibir a notificação
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


