package com.example.whisper.utils.permissions

import android.app.Application
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import javax.inject.Inject

class PermissionChecker @Inject constructor(private val application: Application) {

    fun isPermissionGranted() = ContextCompat.checkSelfPermission(
        application,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}