package com.example.whisper.utils.permissions

/**
 * Base permission class that has to be inherited by all permission type classes
 */
sealed class Permission

class PermissionRequest(val permissions: Array<String>) : Permission()

/**
 * Base permission class that has to be inherited by all permission state classes
 * @param permission the permission name that has to be passed
 */
sealed class PermissionState(val permission: String)

/**
 * Permission goes in Granted State after the user has agreed to proceed
 * @param permission the permission name that has to be passed
 */
class GrantedState(permission: String) : PermissionState(permission)

/**
 * Permission goes in Denied after the user did not agreed to proceed
 * @param permission the permission name that has to be passed
 */
class DeniedState(permission: String) : PermissionState(permission)

/**
 * Permission goes in Show Rationale State and an explanation dialog is
 * shown to the user so he can proceed further
 * @param permission the permission name that has to be passed
 */
class ShowRationaleState(permission: String) : PermissionState(permission)

interface PermissionStateHandler {

    /**
     * When the permission state changes business ui will be executed.
     * Correspondent UI state will be shown to the user
     * @param state the permission state, that will be handled with the appropriate UI state
     */
    fun onPermissionState(state: PermissionState)
}