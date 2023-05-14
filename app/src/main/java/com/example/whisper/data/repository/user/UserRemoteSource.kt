package com.example.whisper.data.repository.user

import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.local.model.toMap
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sendbird.android.SendBird
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class UserRemoteSource @Inject constructor(
    private val auth: FirebaseAuth
) : UserRepository.RemoteSource {

    override suspend fun registerUserFirebase(
        email: String,
        password: String,
        block: (Either<HttpError, String>) -> Unit
    ) {
        // register user in firebase authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val id = task.result?.user?.uid ?: return@addOnCompleteListener
                    block.invoke(Either.right(id))
                } else {
                    block.invoke(Either.left(HttpError(serverMessage = task.exception?.message)))
                }
            }
    }

    override suspend fun registerUserSendbird(
        userModel: UserModel,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        SendBird.connect(userModel.userId) { connectedUser, error ->
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

    override suspend fun loginUserFirebase(
        email: String,
        password: String,
        block: (Either<HttpError, UserModel>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    block.invoke(
                        Either.right(
                            UserModel(
                                userId = task.result.user?.uid ?: EMPTY,
                                email = task.result.user?.email ?: EMPTY,
                                password = EMPTY,
                                username = EMPTY,
                                profilePicture = EMPTY
                            )
                        )
                    )
                    Timber.tag("Firebase Authentication").d("Successfully logged")
                } else {
                    block.invoke(Either.left(HttpError(serverMessage = task.exception?.message)))
                    Timber.tag("Firebase Authentication").e("Failed to login in Firebase")
                }
            }
    }

    override suspend fun connectUserSendbird(
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

    override suspend fun updateUserSendbird(
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

    override suspend fun logout() {
        SendBird.disconnect {
            auth.signOut()
        }
    }
}