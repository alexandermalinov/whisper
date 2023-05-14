package com.example.whisper.data.local

import android.content.Context
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.USER_SHARED_PREFS_IS_SIGNED_IN_KEY
import com.example.whisper.utils.common.USER_SHARED_PREFS_KEY
import com.example.whisper.utils.common.USER_SHARED_PREFS_SIGNED_IN_USER_EMAIL_KEY

object SharedPrefProvider {

    suspend fun setIsUserLoggedIn(context: Context, isUserSignedIn: Boolean) {
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(USER_SHARED_PREFS_IS_SIGNED_IN_KEY, isUserSignedIn)
            .apply()
    }

    suspend fun getIsUserLoggedIn(context: Context) =
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .getBoolean(USER_SHARED_PREFS_IS_SIGNED_IN_KEY, false)

    suspend fun getLoggedInUserEmail(context: Context) =
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .getString(USER_SHARED_PREFS_SIGNED_IN_USER_EMAIL_KEY, EMPTY)

    suspend fun setLoggedInUserEmail(context: Context, email: String?) =
        context.getSharedPreferences(USER_SHARED_PREFS_KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(USER_SHARED_PREFS_SIGNED_IN_USER_EMAIL_KEY, email)
            .apply()
}