package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // -------------------------------------------------------------
    // ✅ SIGN UP
    // -------------------------------------------------------------
    fun signUpEmail(
        email: String,
        password: String,
        fullName: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userData = hashMapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        callback(true, null)
                    }
                    .addOnFailureListener {
                        callback(false, it.localizedMessage)
                    }
            }
            .addOnFailureListener {
                callback(false, it.localizedMessage)
            }
    }

    // -------------------------------------------------------------
    // ✅ LOGIN  (NO CONTEXT HERE)
    // -------------------------------------------------------------
    fun loginEmail(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    // -------------------------------------------------------------
    // FORGOT PASSWORD
    // -------------------------------------------------------------
    fun sendPasswordResetEmail(
        email: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it.localizedMessage) }
    }

    // -------------------------------------------------------------
    // LOGOUT
    // -------------------------------------------------------------
    fun logout() {
        auth.signOut()
    }
}
