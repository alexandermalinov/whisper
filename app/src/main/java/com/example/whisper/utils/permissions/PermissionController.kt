package com.example.whisper.utils.permissions

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ViewDataBinding
import com.example.whisper.ui.base.BasePermissionFragment
import com.example.whisper.utils.permissions.*

/**
 * Executes permission request.
 */
fun onPermissionRequest(
    launcher: ActivityResultLauncher<Array<String>>?,
    permission: Permission
) {
    when (permission) {
        is PermissionRequest -> launcher?.launch(permission.permissions)
    }
}

/**
 * Requests permission and based on the user action [GrantedState], [DeniedState],
 * [ShowRationaleState] corresponded permission state is passed in [onPermissionState]
 * @param onReceivePermissionState is callback in which ui logic will be executed
 */
fun <T : ViewDataBinding> BasePermissionFragment<T>.requestPermission(
    onReceivePermissionState: PermissionStateHandler
) = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
    permissions.forEach { (permission, isGranted) ->
        when {
            isGranted -> GrantedState(permission)
            shouldShowRequestPermissionRationale(permission) -> ShowRationaleState(permission)
            else -> DeniedState(permission)
        }.let { permissionState ->
            onReceivePermissionState.onPermissionState(permissionState)
        }
    }
}