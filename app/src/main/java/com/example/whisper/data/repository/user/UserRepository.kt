package com.example.whisper.data.repository.user

import com.connection.utils.responsehandler.ResponseResultOk
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import javax.inject.Inject

class UserRepository @Inject constructor(private val remote: UserRemoteSource) {

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        fun isSignedIn(block: (Either<HttpError, ResponseResultOk>) -> Unit): Boolean
    }

    fun isSignedIn(block: (Either<HttpError, ResponseResultOk>) -> Unit) {
        remote.isSignedIn(block)
    }
}