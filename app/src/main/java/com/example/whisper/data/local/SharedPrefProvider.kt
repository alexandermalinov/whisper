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
}