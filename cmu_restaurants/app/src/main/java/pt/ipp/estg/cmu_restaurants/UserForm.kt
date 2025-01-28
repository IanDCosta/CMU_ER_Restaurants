package pt.ipp.estg.cmu_restaurants;

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.util.Log
import androidx.core.text.isDigitsOnly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import pt.ipp.estg.cmu_restaurants.Models.User
import pt.ipp.estg.cmu_restaurants.Room.getUserByEmail
import pt.ipp.estg.cmu_restaurants.Room.getUserByPhoneNumber
import pt.ipp.estg.cmu_restaurants.Room.insertUser
import pt.ipp.estg.cmu_restaurants.ui.theme.customAccent
import pt.ipp.estg.cmu_restaurants.ui.theme.customBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserForm(navController: NavController, context: Context) {
    val colors = TextFieldDefaults.outlinedTextFieldColors(
        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
    )

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val numericRegex = Regex("[^0-9]")
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val maxLength = 50

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.customBackground,
        contentColor = MaterialTheme.colorScheme.customAccent,
        topBar = {
            TopAppBar(
                title = { Text("User Profile", color = MaterialTheme.colorScheme.customAccent) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary)
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
                    value = name,
                    onValueChange = {
                        if (it.length <= maxLength) {
                            name = it.replace("\n", "").replace("\r", "")
                        }
                    },
                    label = { Text("Name", color = MaterialTheme.colorScheme.customAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        if (it.length <= maxLength) {
                            email = it.replace("\n", "").replace("\r", "")
                        }
                    },
                    label = { Text("Email", color = MaterialTheme.colorScheme.customAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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
                    label = {
                        Text(
                            "Phone Number",
                            color = MaterialTheme.colorScheme.customAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        if (it.length <= maxLength) {
                            password = it.replace("\n", "").replace("\r", "")
                        }
                    },
                    label = { Text("Password", color = MaterialTheme.colorScheme.customAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()
                        ) {
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

                        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
                        if (!email.matches(emailRegex.toRegex())) {
                            Toast.makeText(context, "Invalid email format.", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        val firebaseAuth = FirebaseAuth.getInstance()

                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val currentUser = firebaseAuth.currentUser

                                    val userId = currentUser?.uid
                                    if (userId != null) {
                                        val db = Firebase.firestore

                                        val newUser = FirebaseAuth.getInstance().currentUser
                                        val uid = newUser?.uid
                                            ?: throw IllegalStateException("User not authenticated")

                                        val user = User(
                                            userId = uid,
                                            name = name,
                                            email = email,
                                            phoneNumber = phoneNumber,
                                            password = password
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                saveUser(user, db, context, navController)
                                            } catch (e: Exception) {
                                                currentUser.delete()
                                                    .addOnCompleteListener { rollbackTask ->
                                                        if (rollbackTask.isSuccessful) {
                                                            Handler(Looper.getMainLooper()).post {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Registration failed: ${e.message}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Registration failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Submit", color = Color.White)
                }

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    )
}

fun saveUser(user: User, db: FirebaseFirestore, context: Context, navController: NavController) =
    CoroutineScope(Dispatchers.IO).launch {
        try {

            val userData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "password" to user.password
            )

            db.collection("users").document(user.userId).set(userData).await()

            saveUserRoomDB(user, context)

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "User Saved Successfuly", Toast.LENGTH_LONG).show()
                navController.navigate("login")
            }
        } catch (e: Exception) {
            val newUser = FirebaseAuth.getInstance().currentUser
            val uid = newUser?.uid
            if (uid != null) {
                db.collection("users").document(uid).delete().await()
            }

            withContext(Dispatchers.Main) {
                Log.println(Log.DEBUG, "Log", e.message.toString());
                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

fun saveUserRoomDB(user: User, context: Context) {
    val existingUserByEmail = getUserByEmail(context, user.email)
    if (existingUserByEmail != null) {
        throw IllegalStateException("Email already in use.")
    }

    /*val existingUserByPhone = getUserByPhoneNumber(context, user.phoneNumber)
    if (existingUserByPhone != null) {
        throw IllegalStateException("Phone number already in use.")
    }*/

    insertUser(context, user)
}