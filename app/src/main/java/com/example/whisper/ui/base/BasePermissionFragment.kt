package com.example.whisper.ui.base

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.ViewDataBinding
import com.example.whisper.utils.common.collectLatestFlow
import com.example.whisper.utils.permissions.Permission
import com.example.whisper.utils.permissions.PermissionStateHandler
import com.example.whisper.utils.permissions.onPermissionRequest
import com.example.whisper.utils.permissions.requestPermission
import kotlinx.coroutines.flow.SharedFlow

abstract class BasePermissionFragment<T : ViewDataBinding> : BaseFragment<T>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addListener()
    }

    /* --------------------------------------------------------------------------------------------
     * Abstract
    ---------------------------------------------------------------------------------------------*/
    abstract fun providePermissionStateHandler(): PermissionStateHandler?

    /* --------------------------------------------------------------------------------------------
     * Protected
    ---------------------------------------------------------------------------------------------*/
    protected fun collectPermission(permissionFlow: SharedFlow<Permission>) {
        collectLatestFlow(permissionFlow) { permission ->
            onPermissionRequest(requestPermissionLauncher, permission)
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun addListener() {
        providePermissionStateHandler()?.let { permissionHandler ->
            requestPermissionLauncher = requestPermission(permissionHandler)
        }
    }
}