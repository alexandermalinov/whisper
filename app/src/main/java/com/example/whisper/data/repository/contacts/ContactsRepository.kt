package com.example.whisper.data.repository.contacts

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.sendbird.android.GroupChannel
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
}