package com.example.whisper.data.repository.user

import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.entity.toUserModel
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.local.model.toUser
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remote: UserRemoteSource,
    private val local: UserLocalSource,
    var cachedUser: UserModel
) {

    init {
        CoroutineScope(SupervisorJob()).launch {
            val loggedUser = local.getLoggedUser()
            if (loggedUser != null) cachedUser = loggedUser.toUserModel()
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun registerUserFirebase(email: String, password: String): Either<HttpError, String>

        suspend fun registerUserSendbird(userModel: UserModel): Either<HttpError, ResponseResultOk>

        suspend fun loginUserFirebase(email: String, password: String): Either<HttpError, UserModel>

        suspend fun connectUserSendbird(userId: String): Either<HttpError, com.sendbird.android.User>

        suspend fun updateUserSendbird(
            username: String,
            profilePictureFile: File
        ): Either<HttpError, ResponseResultOk>

        suspend fun getCurrentUserId(): String

        suspend fun logout()
    }

    interface LocalSource {

        suspend fun updateUser(user: UserModel)

        suspend fun registerUser(user: User)

        suspend fun getUser(userId: String): User?

        suspend fun getLoggedUser(): User?

        suspend fun setIsUserLoggedIn(isSignedIn: Boolean)

        suspend fun isUserLoggedIn(): Boolean

        suspend fun setLoggedInUserEmail(email: String)

        suspend fun logout()
    }

    suspend fun registerUserFirebase(
        email: String,
        password: String
    ): Either<HttpError, String> = remote.registerUserFirebase(email, password)

    suspend fun loginUserFirebase(
        email: String,
        password: String
    ): Either<HttpError, UserModel> = remote.loginUserFirebase(email, password)

    suspend fun registerUserLocalDB(userModel: UserModel) {
        local.registerUser(userModel.toUser())
        loginUserLocalDB(userModel)
    }

    suspend fun updateUserLocalDB(userModel: UserModel) {
        local.updateUser(userModel)
        cachedUser = userModel
    }

    suspend fun loginUserLocalDB(userModel: UserModel) {
        local.setIsUserLoggedIn(true)
        local.setLoggedInUserEmail(userModel.email)
        cachedUser = userModel
        if (local.getUser(userModel.userId) == null) {
            local.registerUser(user = userModel.toUser())
        }
    }

    suspend fun registerUserSendbird(userModel: UserModel): Either<HttpError, ResponseResultOk> =
        remote.registerUserSendbird(userModel)

    suspend fun connectUserSendbird(userId: String): Either<HttpError, com.sendbird.android.User> =
        remote.connectUserSendbird(userId)

    suspend fun updateUserSendbird(
        username: String,
        profilePictureFile: File
    ): Either<HttpError, ResponseResultOk> = remote.updateUserSendbird(username, profilePictureFile)

    suspend fun isUserLoggedIn() = local.isUserLoggedIn()

    suspend fun getLoggedUser(): User? = local.getLoggedUser()

    suspend fun getLoggedUserId() = local.getLoggedUser()?.userId

    suspend fun logout() {
        remote.logout()
        local.logout()
        local.updateUser(cachedUser)
    }
}