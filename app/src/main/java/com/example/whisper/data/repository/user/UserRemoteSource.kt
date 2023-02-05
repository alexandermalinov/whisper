package com.example.whisper.data.repository.user

import com.example.whisper.data.remote.model.user.UserModel
import com.example.whisper.data.remote.model.user.toMap
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.google.firebase.auth.FirebaseAuth
import com.google.rpc.context.AttributeContext.Auth
import com.sendbird.android.SendBird
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UserRemoteSource @Inject constructor(
    private val auth: FirebaseAuth
) : UserRepository.RemoteSource {

    override suspend fun registerFirebaseAuth(
        email: String,
        password: String,
        block: (Either<HttpError, String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    block.invoke(Either.right(task.result?.user?.uid ?: EMPTY))
                } else {
                    block.invoke(Either.left(HttpError(serverMessage = task.exception?.message)))
                }
            }
    }

    override suspend fun registerSendbird(
        userModel: UserModel,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        SendBird.connect(userModel.id) { connectedUser, error ->
            if (error != null || connectedUser == null) {
                block.invoke(Either.left(HttpError(serverMessage = error.message)))
            } else {
                SendBird.getCurrentUser().createMetaData(userModel.toMap()) { user, error ->
                    if (error == null) {
                        block.invoke(Either.right(ResponseResultOk))
                    } else {
                        block.invoke(Either.left(HttpError(serverMessage = error.message)))
                    }
                }
            }
        }
    }

    override suspend fun loginFirebaseAuth(
        email: String,
        password: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    block.invoke(Either.right(ResponseResultOk))
                    Timber.tag("Firebase Authentication").d("Successfully logged")
                } else {
                    block.invoke(Either.left(HttpError(serverMessage = task.exception?.message)))
                    Timber.tag("Firebase Authentication").e("Failed to login in Firebase")
                }
            }
    }

    override suspend fun connectToSendbird(
        userId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        SendBird.connect(userId) { connectedUser, error ->
            if (error != null || connectedUser == null) {
                block.invoke(Either.left(HttpError(serverMessage = error.message)))
            } else {
                block.invoke(Either.right(ResponseResultOk))
            }
        }
    }

    override suspend fun getCurrentUserId() = auth.currentUser?.uid ?: EMPTY

    override suspend fun updateRemoteUser(
        username: String,
        profilePictureFile: File,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        SendBird.updateCurrentUserInfoWithProfileImage(
            username,
            profilePictureFile
        ) { exception ->
            if (exception != null) {
                block.invoke(Either.left(HttpError(serverMessage = exception.message)))
            } else {
                block.invoke(Either.right(ResponseResultOk))
            }
        }
    }
}