package com.example.whisper.data.repository.user

import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.entity.toUserModel
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.local.model.toUser
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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
            val loggedUser = getLoggedUser()
            if (loggedUser != null) cachedUser = loggedUser.toUserModel()
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun registerUserFirebase(
            email: String,
            password: String,
            block: (Either<HttpError, String>) -> Unit
        )

        suspend fun registerUserSendbird(
            userModel: UserModel,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun loginUserFirebase(
            email: String,
            password: String,
            block: (Either<HttpError, UserModel>) -> Unit
        )

        suspend fun connectUserSendbird(
            userId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun updateUserSendbird(
            username: String,
            profilePictureFile: File,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun getCurrentUserId(): String

        suspend fun logout()
    }

    interface LocalSource {

        suspend fun updateUser(user: UserModel)

        suspend fun registerUser(user: User)

        suspend fun getUser(userId: String): User?

        suspend fun getLoggedUser(): User?

        fun getPinnedContacts(email: String): Flow<List<ContactModel>>

        suspend fun setIsUserLoggedIn(isSignedIn: Boolean)

        suspend fun isUserLoggedIn(): Boolean

        suspend fun setLoggedInUserEmail(email: String)

        suspend fun logout()
    }

    suspend fun registerUserFirebase(
        email: String,
        password: String,
        block: (Either<HttpError, String>) -> Unit
    ) {
        remote.registerUserFirebase(email, password, block)
    }

    suspend fun loginUserFirebase(
        email: String,
        password: String,
        block: (Either<HttpError, UserModel>) -> Unit
    ) {
        remote.loginUserFirebase(email, password, block)
    }

    suspend fun registerUserLocalDB(user: User) {
        local.registerUser(user)
        cachedUser = user.toUserModel()
        loginUserLocalDB(user.email)
    }

    suspend fun updateUserLocalDB(userModel: UserModel?) {
        userModel?.let {
            //local.registerUser(userModel.toUser())
            cachedUser = userModel
        }
    }

    suspend fun loginUserLocalDB(email: String, id: String = EMPTY) {
        local.setIsUserLoggedIn(true)
        local.setLoggedInUserEmail(email)
        if (local.getUser(id) == null) {
            local.registerUser(User(userId = id, email = email))
        }
    }

    suspend fun registerUserSendbird(
        userModel: UserModel,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.registerUserSendbird(userModel, block)
    }

    suspend fun connectUserSendbird(
        userId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.connectUserSendbird(userId, block)
    }

    suspend fun updateUserSendbird(
        username: String,
        profilePictureFile: File,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.updateUserSendbird(username, profilePictureFile, block)
    }

    suspend fun isUserLoggedIn() = local.isUserLoggedIn()

    suspend fun getLoggedUser(): User? = local.getLoggedUser()

    suspend fun getLoggedUserId() = local.getLoggedUser()?.userId

    fun getPinnedContacts(): Flow<List<ContactModel>> = local.getPinnedContacts(cachedUser.email)

    suspend fun logout() {
        remote.logout()
        local.logout()
        cachedUser = cachedUser.copy(pinnedContacts = emptyList())
        local.updateUser(cachedUser)
    }
}