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
import pt.ipp.estg.cmu_restaurants.Room.clearUserTable
import pt.ipp.estg.cmu_restaurants.Room.getUserByEmail
import pt.ipp.estg.cmu_restaurants.Room.getUserByPhoneNumber
import pt.ipp.estg.cmu_restaurants.Room.insertUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserForm(navController: NavController, context: Context) {
    val backgroundColor = Color(0xFF3C0A3D)
    val accentColor = Color(0xFFE94D9B)
    val textColor = Color.White
    val colors = TextFieldDefaults.outlinedTextFieldColors(
        focusedLabelColor = accentColor,
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = accentColor,
        unfocusedBorderColor = Color.Gray,
    )

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(0) }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentColor = textColor,
        topBar = {
            TopAppBar(
                title = { Text("User Profile", color = Color.White) },
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = phoneNumber.toString(),
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            phoneNumber = it.toInt()
                            phoneError = false
                        } else {
                            phoneError = true
                        }
                    },
                    label = { Text("Phone Number", color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = colors,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isEmpty() || email.isEmpty() || phoneNumber.toString()
                                .isEmpty() || password.isEmpty()
                        ) {
                            Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94D9B))
                ) {
                    Text(text = "Submit", color = Color.White)
                }

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            clearUserTable(context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94D9B))
                ) {
                    Text(text = "Clear Table", color = Color.White)
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

    val existingUserByPhone = getUserByPhoneNumber(context, user.phoneNumber)
    if (existingUserByPhone != null) {
        throw IllegalStateException("Phone number already in use.")
    }

    insertUser(context, user)
}