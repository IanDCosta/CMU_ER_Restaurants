package pt.ipp.estg.cmu_restaurants.Models


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Attempt to sign in
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    val userId = user.uid
                    println("User logged in successfully: $userId")

                    //search in firestore
                    val userDocument = firestore.collection("users").document(userId).get().await()

                    if (userDocument != null) {
                        onResult(true, userId) // Found in "users"
                    }
                } else {
                    println("Login failed: user is null")
                    onResult(false, null) // Login failed
                }
            } catch (e: Exception) {
                println("Error during login: ${e.message}")
                e.printStackTrace()
                onResult(false, null) // Error occurred
            }
        }
    }

    // Função para registro
    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                if (result != null && result.user != null) {
                    onResult(true) // Registro bem-sucedido
                } else {
                    onResult(false) // Falha no registro
                }
            } catch (e: Exception) {
                Log.e("Auth", e.message+"")
                onResult(false) // Erro no registro
            }
        }
    }

    fun updateUser(userId: String, updatedUser: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).set(updatedUser).await()
                onResult(true) // Atualização bem-sucedida
            } catch (e: Exception) {
                Log.e("Auth", e.message+"")
                onResult(false) // Erro na atualização
            }
        }
    }

}