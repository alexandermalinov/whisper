package com.example.whisper.data.repository.contacts

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.sendbird.android.GroupChannel
import com.sendbird.android.User
import javax.inject.Inject

class ContactsRepository @Inject constructor(private val remote: RemoteSource) {

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun getContacts(
            filter: ConnectionStatus,
            block: (Either<HttpError, List<GroupChannel>>) -> Unit
        )

        suspend fun getNotConnectedUsers(
            block: (Either<HttpError, List<User>>) -> Unit
        )
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
     ---------------------------------------------------------------------------------------------*/
    suspend fun getContacts(
        filter: ConnectionStatus,
        block: (Either<HttpError, List<GroupChannel>>) -> Unit
    ) {
        remote.getContacts(filter, block)
    }

    suspend fun getUserNotConnectedWith(
        block: (Either<HttpError, List<User>>) -> Unit
    ) {
        remote.getNotConnectedUsers(block)
    }
}