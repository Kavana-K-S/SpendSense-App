package com.example.data

import android.content.Context

object NotificationRepository {

    suspend fun saveNotification(
        context: Context,
        title: String,
        message: String,
        type: String // "WARNING" or "INFO"
    ) {
        AppDatabase.getDatabase(context)
            .notificationDao()
            .insert(
                NotificationEntity(
                    title = title,
                    message = message,
                    type = type
                )
            )
    }
}
