package com.example.whisper.data.local

import android.content.Context
import com.example.whisper.utils.common.USER_SHARED_PREFS_IS_FIRST_TIME_KEY
import com.example.whisper.utils.common.USER_SHARED_PREFS_IS_SIGNED_IN_KEY
import com.example.whisper.utils.common.USER_SHARED_PREFS_KEY

object SharedPrefProvider {

    suspend fun setIsUserSignedIn(context: Context, isUserSignedIn: Boolean) {
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(USER_SHARED_PREFS_IS_SIGNED_IN_KEY, isUserSignedIn)
            .apply()
    }

    suspend fun getIsUserSignedIn(context: Context) =
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .getBoolean(USER_SHARED_PREFS_IS_SIGNED_IN_KEY, false)

    /**
     * @param isFirstTime determines whether it is first time opening the app
     * Sets whether it is the first time opening the app or not.
     * If yes, the user will be navigated to Sign In screen
     */
    suspend fun setIsFirstTime(context: Context, isFirstTime: Boolean) {
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(USER_SHARED_PREFS_IS_FIRST_TIME_KEY, isFirstTime)
            .apply()
    }

    /**
     * Gets whether it is the first time opening the app or not
     * If yes, the user will be navigated to Sign In screen
     */
    suspend fun getIsFirstTime(context: Context) =
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .getBoolean(USER_SHARED_PREFS_IS_FIRST_TIME_KEY, true)
}