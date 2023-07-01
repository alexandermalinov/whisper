package com.example.whisper.domain.signin

import com.example.whisper.data.local.model.toUserModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.PopulateContactsUseCase
import com.example.whisper.utils.NetworkHandler

class SignInUseCase(
    val userRepository: UserRepository,
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository,
    private val networkHandler: NetworkHandler
) {

    suspend operator fun invoke(email: String, password: String): SignInState {
        if (email.isEmpty() || password.isEmpty()) return SignInState.CredentialsErrorState

        val signInResult = userRepository.loginUserFirebase(email, password)

        return signInResult.foldSuspend({
            if (networkHandler.isNetworkAvailable())
                SignInState.CredentialsErrorState
            else
                SignInState.NetworkErrorState
        }, { userModel ->
            val connectResult = userRepository.connectUserSendbird(userModel.userId)

            connectResult.foldSuspend({
                if (networkHandler.isNetworkAvailable()) {
                    SignInState.ErrorState
                } else {
                    SignInState.NetworkErrorState
                }
            }, { user ->
                userRepository.loginUserLocalDB(user.toUserModel())

                PopulateContactsUseCase(
                    contactsRepository,
                    recentChatsRepository
                ).invoke(userModel.userId)

                SignInState.SuccessState
            })
        })
    }
}

sealed class SignInState {
    object NetworkErrorState : SignInState()
    object CredentialsErrorState : SignInState()
    object ErrorState : SignInState()
    object SuccessState : SignInState()
}