package com.example.whisper.data.repository.user

import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.local.model.toMap
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.google.firebase.auth.FirebaseAuth
import com.sendbird.android.SendBird
import com.sendbird.android.User
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRemoteSource @Inject constructor(
    private val auth: FirebaseAuth
) : UserRepository.RemoteSource {

    override suspend fun registerUserFirebase(
        email: String,
        password: String,
    ): Either<HttpError, String> = suspendCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val id = task.result?.user?.uid ?: return@addOnCompleteListener
                    continuation.resume(Either.right(id))
                } else {
                    continuation.resume(Either.left(HttpError(serverMessage = task.exception?.message)))
                }
            }
    }

    override suspend fun registerUserSendbird(
        userModel: UserModel
    ): Either<HttpError, ResponseResultOk> = suspendCoroutine { continuation ->
        SendBird.connect(userModel.userId) { connectedUser, error ->
            if (error != null || connectedUser == null) {
                continuation.resume(Either.left(HttpError(serverMessage = error.message)))
            } else {
                SendBird.getCurrentUser().createMetaData(userModel.toMap()) { user, error ->
                    if (error == null) {
                        continuation.resume(Either.right(ResponseResultOk))
                    } else {
                        continuation.resume(Either.left(HttpError(serverMessage = error.message)))
                    }
                }
            }
        }
    }

    override suspend fun loginUserFirebase(
        email: String,
        password: String
    ): Either<HttpError, UserModel> = suspendCoroutine { continuation ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userModel = UserModel(
                        userId = task.result.user?.uid ?: EMPTY,
                        email = task.result.user?.email ?: EMPTY,
                        password = EMPTY,
                        username = EMPTY,
                        profilePicture = EMPTY
                    )
                    continuation.resume(Either.right(userModel))
                    Timber.tag("Firebase Authentication").d("Successfully logged")
                } else {
                    val httpError = HttpError(serverMessage = task.exception?.message)
                    continuation.resume(Either.left(httpError))
                    Timber.tag("Firebase Authentication").e("Failed to login in Firebase")
                }
            }
    }

    override suspend fun connectUserSendbird(
        userId: String
    ): Either<HttpError, User> = suspendCoroutine { continuation ->
        SendBird.connect(userId) { connectedUser, error ->
            if (error != null || connectedUser == null) {
                continuation.resume(Either.left(HttpError(serverMessage = error.message)))
            } else {
                continuation.resume(Either.right(connectedUser))
            }
        }
    }

    override suspend fun getCurrentUserId() = auth.currentUser?.uid ?: EMPTY

    override suspend fun updateUserSendbird(
        username: String,
        profilePictureFile: File
    ): Either<HttpError, ResponseResultOk> = suspendCoroutine { continuation ->
        SendBird.updateCurrentUserInfoWithProfileImage(
            username,
            profilePictureFile
        ) { exception ->
            if (exception != null) {
                continuation.resume(Either.left(HttpError(serverMessage = exception.message)))
            } else {
                continuation.resume(Either.right(ResponseResultOk))
            }
        }
    }

    override suspend fun logout() {
        SendBird.disconnect {
            auth.signOut()
        }
    }
}