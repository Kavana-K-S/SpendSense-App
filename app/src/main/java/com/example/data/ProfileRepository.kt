package com.example.data

import android.content.Context

class ProfileRepository {

    suspend fun loadProfile(
        context: Context,
        userId: String
    ): ProfileEntity? {
        return AppDatabase
            .getDatabase(context)
            .profileDao()
            .getProfile(userId)
    }

    suspend fun saveProfile(
        context: Context,
        profile: ProfileEntity
    ) {
        AppDatabase
            .getDatabase(context)
            .profileDao()
            .saveProfile(profile)
    }
}
