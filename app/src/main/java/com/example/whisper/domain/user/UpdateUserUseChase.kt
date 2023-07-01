package com.example.whisper.domain.user

import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.NetworkHandler
import com.sendbird.android.SendBird
import java.io.File

class UpdateUserUseChase(
    private val userRepository: UserRepository,
    private val networkHandler: NetworkHandler
) {

    suspend operator fun invoke(username: String, pictureFile: File): UpdateUserState {
        if (username.isEmpty() || pictureFile.path.isEmpty()) return UpdateUserState.ErrorState

        val result = userRepository.updateUserSendbird(username, pictureFile)
        return result.foldSuspend(
            { onFailure ->
                if (networkHandler.isNetworkAvailable()) {
                    UpdateUserState.NetworkErrorState
                } else {
                    UpdateUserState.ErrorState
                }
            },
            { onSuccess ->
                val currentUser = SendBird.getCurrentUser()
                val userModel = userRepository.cachedUser.copy(
                    username = currentUser.nickname,
                    profilePicture = currentUser.profileUrl
                )

                userRepository.updateUserLocalDB(userModel)
                UpdateUserState.SuccessState
            }
        )
    }
}

sealed class UpdateUserState {
    object NetworkErrorState : UpdateUserState()
    object ErrorState : UpdateUserState()
    object SuccessState : UpdateUserState()
}