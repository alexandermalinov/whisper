package com.example.whisper.data.repository.user

import com.connection.utils.responsehandler.ResponseResultOk
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import javax.inject.Inject

class UserRemoteSource @Inject constructor() : UserRepository.RemoteSource {

    override suspend fun register() {
        TODO("Not yet implemented")
    }

}