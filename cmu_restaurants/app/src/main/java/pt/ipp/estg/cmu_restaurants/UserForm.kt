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
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import pt.ipp.estg.cmu_restaurants.Models.User

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
                                    if(userId != null) {
                                        val db = Firebase.firestore

                                        val newUser = User(
                                            name = name,
                                            email = email,
                                            phoneNumber = phoneNumber,
                                            password = password
                                        )

                                        saveUser(newUser, db, context)
                                    }
                                } else {
                                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }

//                        val existingUserByEmail = getUserByEmail(context, email)
//                        if (existingUserByEmail != null) {
//                            Toast.makeText(context, "Email already in use.", Toast.LENGTH_SHORT)
//                                .show()
//                            return@Button
//                        }
//
//                        val existingUserByPhone =
//                            getUserByPhoneNumber(context, phoneNumber)
//                        if (existingUserByPhone != null) {
//                            Toast.makeText(
//                                context,
//                                "Phone number already in use.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            return@Button
//                        }
//
//                        val user = User(
//                            name = name,
//                            email = email,
//                            phoneNumber = phoneNumber,
//                            password = password
//                        )
//                        insertUser(context, user)
//                        val insertedUser = getUserByEmail(context, email)
//                        message = if (insertedUser != null) ({
//                            "User inserted successfully!"
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        navController.navigate("login")
//                        }).toString() else ({
//                            message = "Failed to insert user."
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                        }).toString()
//                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94D9B))
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

fun saveUser(user: User, db: FirebaseFirestore, context: Context) = CoroutineScope(Dispatchers.IO).launch {
    try{
        val newUser = FirebaseAuth.getInstance().currentUser
        val uid = newUser?.uid ?: throw IllegalStateException("User not authenticated")

        val userData = hashMapOf(
            "name" to user.name,
            "email" to user.email,
            "phoneNumber" to user.phoneNumber,
            "password" to user.password
        )

        db.collection("users").document(uid).set(userData).await()
        withContext(Dispatchers.Main){
            Toast.makeText(context, "User Saved Successfuly", Toast.LENGTH_LONG).show()
        }
    }catch(e: Exception){
        withContext(Dispatchers.Main){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}