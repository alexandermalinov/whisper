package com.example.whisper.data.repository.user

import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remote: UserRemoteSource,
    private val local: UserLocalSource
) {

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun register()
    }

    interface LocalSource {

        suspend fun setIsSignedIn(isSignedIn: Boolean)

        suspend fun isSignedIn(): Boolean

        suspend fun setIsFirstTime(isFirstTime: Boolean)

        suspend fun isFirstTime(): Boolean
    }

    suspend fun setIsSignedIn(isSignedIn: Boolean) {
        local.setIsSignedIn(isSignedIn)
    }

    suspend fun isSignedIn() = local.isSignedIn()

    suspend fun setIsFirstTime(isFirstTime: Boolean) {
        local.setIsFirstTime(isFirstTime)
    }

    suspend fun isFirstTime() = local.isFirstTime()
}