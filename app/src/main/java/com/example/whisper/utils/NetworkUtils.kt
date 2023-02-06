package com.example.whisper.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager

fun Context.isNetworkAvailable() = (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
    .let { connectManager ->
        connectManager.getNetworkCapabilities(connectManager.activeNetwork) != null
    }