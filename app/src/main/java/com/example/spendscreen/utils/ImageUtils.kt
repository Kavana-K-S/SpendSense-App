package com.example.spendscreen.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

object ImageUtils {

    // ------------------------------------------------------------------
    // ⭐ Convert Base64 → ImageBitmap (Still kept for compatibility)
    // ------------------------------------------------------------------
    fun imageFromBase64(base64: String): ImageBitmap {
        val decoded = Base64.decode(base64, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        return bmp.asImageBitmap()
    }

    // ------------------------------------------------------------------
    // ⭐ Convert Bitmap → Base64 (Optional use)
    // ------------------------------------------------------------------
    fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    // ------------------------------------------------------------------
    // ⭐ Upload image to Firebase Storage
    // Returns: public download URL (String)
    // ------------------------------------------------------------------
    suspend fun uploadImageToFirebase(userId: String, bitmap: Bitmap): String {
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("profile_images/$userId.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data).await()  // upload completed

        return storageRef.downloadUrl.await().toString()
    }

    // ------------------------------------------------------------------
    // ⭐ Save image URL in Firestore → users/{userId}/profileImage
    // ------------------------------------------------------------------
    suspend fun saveImageUrlToFirestore(userId: String, imageUrl: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(userId)
            .update(mapOf("profileImage" to imageUrl))
            .await()
    }

    // ------------------------------------------------------------------
    // ⭐ Load image from Firebase URL → ImageBitmap
    // ------------------------------------------------------------------
    suspend fun loadImageBitmapFromUrl(imageUrl: String): ImageBitmap? {
        return try {
            val inputStream = java.net.URL(imageUrl).openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ------------------------------------------------------------------
    // ⭐ Convert URI → Bitmap (used when user picks image)
    // ------------------------------------------------------------------
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
