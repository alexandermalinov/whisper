package com.example.whisper.data.repository.user

import com.connection.utils.responsehandler.ResponseResultOk
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import javax.inject.Inject

class UserRemoteSource @Inject constructor() : UserRepository.RemoteSource {

    override fun isSignedIn(block: (Either<HttpError, ResponseResultOk>) -> Unit): Boolean {
        TODO("Not yet implemented")
    }
}