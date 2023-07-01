package com.example.whisper.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import javax.inject.Inject

class NetworkHandler @Inject constructor(private val application: Application) {

    fun isNetworkAvailable(): Boolean {
        val connectManager = (application.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

        return connectManager.getNetworkCapabilities(connectManager.activeNetwork) != null
    }
}