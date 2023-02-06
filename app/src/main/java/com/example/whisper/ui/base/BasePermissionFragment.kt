package com.example.whisper.ui.base

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import com.example.whisper.utils.permissions.Permission
import com.example.whisper.utils.permissions.PermissionStateHandler
import com.example.whisper.utils.permissions.onPermissionRequest
import com.example.whisper.utils.permissions.requestPermission

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
    protected fun observePermissionData(permissionLiveData: LiveData<Permission>) {
        permissionLiveData.observe(viewLifecycleOwner) { permission ->
            requestPermissionLauncher?.let {
                onPermissionRequest(it, permission)
            }
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