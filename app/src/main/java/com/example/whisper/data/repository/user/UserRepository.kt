package com.example.whisper.data.repository.user

import com.example.whisper.data.local.entity.User
import com.example.whisper.data.remote.model.user.UserModel
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import java.io.File
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remote: UserRemoteSource,
    private val local: UserLocalSource
) {

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun registerFirebaseAuth(
            email: String,
            password: String,
            block: (Either<HttpError, String>) -> Unit
        )

        suspend fun registerSendbird(
            userModel: UserModel,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun loginFirebaseAuth(
            email: String,
            password: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun connectToSendbird(
            userId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun getCurrentUserId(): String

        suspend fun updateRemoteUser(
            username: String,
            profilePictureFile: File,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun logout()
    }

    interface LocalSource {

        suspend fun registerUser(user: User)

        suspend fun getLoggedUser(id: String): User

        suspend fun setIsSignedIn(isSignedIn: Boolean)

        suspend fun isSignedIn(): Boolean

        suspend fun logout()
    }

    suspend fun registerFirebaseAuth(
        email: String,
        password: String,
        block: (Either<HttpError, String>) -> Unit
    ) {
        remote.registerFirebaseAuth(email, password, block)
    }

    suspend fun loginFirebaseAuth(
        email: String,
        password: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.loginFirebaseAuth(email, password, block)
    }

    suspend fun connectUser(
        userModel: UserModel,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.registerSendbird(userModel, block)
    }

    suspend fun connectToSendbird(
        userId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.connectToSendbird(userId, block)
    }

    suspend fun getLoggedUserId() = remote.getCurrentUserId()

    suspend fun updateRemoteUser(
        username: String,
        profilePictureFile: File,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.updateRemoteUser(username, profilePictureFile, block)
    }

    suspend fun registerUserDB(user: User) {
        local.registerUser(user)
    }

    suspend fun setIsSignedIn(isSignedIn: Boolean) {
        local.setIsSignedIn(isSignedIn)
    }

    suspend fun isSignedIn() = local.isSignedIn()

    suspend fun getLoggedUser(id: String): User = local.getLoggedUser(id)

    suspend fun logout() {
        remote.logout()
        local.setIsSignedIn(false)
    }
}