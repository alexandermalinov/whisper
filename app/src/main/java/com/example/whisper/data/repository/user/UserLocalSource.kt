package com.example.whisper.data.repository.user

import android.content.Context
import com.example.whisper.data.local.SharedPrefProvider
import javax.inject.Inject

class UserLocalSource @Inject constructor(
    private val context: Context
) : UserRepository.LocalSource {

    override suspend fun setIsSignedIn(isSignedIn: Boolean) {
        SharedPrefProvider.setIsUserSignedIn(context, isSignedIn)
    }

    override suspend fun isSignedIn(): Boolean = SharedPrefProvider.getIsUserSignedIn(context)

    override suspend fun setIsFirstTime(isFirstTime: Boolean) {
        SharedPrefProvider.setIsFirstTime(context, isFirstTime)
    }

    override suspend fun isFirstTime(): Boolean = SharedPrefProvider.getIsFirstTime(context)
}