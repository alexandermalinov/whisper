package com.example.whisper.domain.signup

import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.PopulateContactsUseCase
import com.example.whisper.utils.NetworkHandler
import com.example.whisper.utils.common.EMPTY

class SignUpUseCase(
    val userRepository: UserRepository,
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository,
    private val networkHandler: NetworkHandler
) {

    suspend operator fun invoke(email: String, password: String): SignUpState {
        if (email.isEmpty() || password.isEmpty()) return SignUpState.CredentialsErrorState

        val registerUserFirebaseResult = userRepository.registerUserFirebase(email, password)
        var userModel: UserModel

        return registerUserFirebaseResult.foldSuspend({ onFailure ->
            if (networkHandler.isNetworkAvailable())
                SignUpState.CredentialsErrorState
            else
                SignUpState.NetworkErrorState
        }, { userId ->
            userModel = UserModel(
                userId = userId,
                email = email,
                password = password,
                username = EMPTY,
                profilePicture = EMPTY
            )

            val result = userRepository.registerUserSendbird(userModel)

            result.foldSuspend({ onFailure ->
                if (networkHandler.isNetworkAvailable()) {
                    SignUpState.NetworkErrorState
                } else {
                    SignUpState.CredentialsErrorState
                }
            }, { onSuccess ->
                userRepository.loginUserLocalDB(userModel)

                PopulateContactsUseCase(contactsRepository, recentChatsRepository)
                    .invoke(userRepository.cachedUser.userId)

                SignUpState.SuccessState
            })
        })
    }
}


sealed class SignUpState {
    object NetworkErrorState : SignUpState()
    object CredentialsErrorState : SignUpState()
    object ErrorState : SignUpState()
    object SuccessState : SignUpState()
}