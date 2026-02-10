package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import com.example.data.SessionManager
import android.content.Context


object FirebaseHelper {


    private val db = FirebaseFirestore.getInstance()


    fun loadProfileImage(
        context: Context,
        onResult: (String?) -> Unit
    ) {
        val userId = SessionManager(context).getUserId()
        if (userId == null) {
            onResult(null)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                onResult(doc.getString("profileImage"))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}
