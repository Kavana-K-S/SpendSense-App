package com.example.spendscreen.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.analytics.FirebaseAnalytics
import android.os.Bundle

object NotificationHelper {

    private const val CHANNEL_ID = "budget_alerts_channel"
    private const val CHANNEL_NAME = "Budget Alerts"
    private const val CHANNEL_DESC = "Notifications about budget limits"

    // -------------------------------------------------------------
    // ⭐ CREATE NOTIFICATION CHANNEL
    // -------------------------------------------------------------
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            ch.description = CHANNEL_DESC

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }

    // -------------------------------------------------------------
    // ⭐ DISPLAY NOTIFICATION + LOG TO FIREBASE
    // -------------------------------------------------------------
    fun notify(
        context: Context,
        userId: String?,
        title: String,
        body: String,
        notificationId: Int
    ) {
        createChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())

        // -------------------------------------------------------------
        // ⭐ OPTIONAL: Log to Firebase Firestore for history
        // -------------------------------------------------------------
        if (!userId.isNullOrEmpty()) {
            val data = mapOf(
                "title" to title,
                "message" to body,
                "timestamp" to System.currentTimeMillis()
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .add(data)
                .addOnSuccessListener { }
                .addOnFailureListener { }
        }

        // -------------------------------------------------------------
        // ⭐ OPTIONAL: Log to Firebase Analytics
        // -------------------------------------------------------------
        try {
            val analytics = FirebaseAnalytics.getInstance(context)
            val bundle = Bundle()
            bundle.putString("notification_title", title)
            bundle.putString("notification_body", body)
            analytics.logEvent("budget_notification_triggered", bundle)
        } catch (_: Exception) { }
    }
}
