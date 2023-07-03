package com.example.whisper.data.repository.recentchats

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecentChatsRepository @Inject constructor(
    private val remote: RemoteSource,
    private val local: LocalSource
) {

    var cachedRecentChats: HashMap<String, ContactModel> = hashMapOf()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        coroutineScope.launch {
            cachedRecentChats = local.getAllRecentChats().associateBy { it.contactUrl } as HashMap
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun deleteRecentChat(
            id: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun muteRecentChat(
            channelId: String,
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun unmuteRecentChat(
            channelId: String,
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun pinRecentChat(
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun unpinRecentChat(
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )
    }

    interface LocalSource {

        suspend fun updateRecentChat(recentChat: ContactModel)

        fun getRecentChatsFlow(): Flow<List<ContactModel>>

        suspend fun getAllRecentChats(): List<ContactModel>

        suspend fun addRecentChat(recentChat: ContactModel)

        suspend fun addRecentChats(recentChats: List<ContactModel>)

        suspend fun getRecentChat(recentChatUrl: String): ContactModel?

        suspend fun deleteRecentChat(recentChat: ContactModel)

        suspend fun deleteAllRecentChat()

        suspend fun muteRecentChat(recentChat: ContactModel)

        suspend fun unmuteRecentChat(recentChat: ContactModel)

        suspend fun pinRecentChat(recentChat: ContactModel)

        suspend fun unpinRecentChat(recentChat: ContactModel)
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
     ---------------------------------------------------------------------------------------------*/

    suspend fun deleteRecentChat(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.deleteRecentChat(id, block)
    }

    suspend fun muteRecentChat(
        channelId: String,
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.muteRecentChat(channelId, contactId, block)
    }

    suspend fun unmuteRecentChat(
        channelId: String,
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.unmuteRecentChat(channelId, contactId, block)
    }

    suspend fun pinRecentChat(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.pinRecentChat(contactId, block)
    }

    suspend fun unpinRecentChat(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.unpinRecentChat(contactId, block)
    }

    fun getRecentChatsDbFlow() = local.getRecentChatsFlow()

    suspend fun getRecentChatsDb() = local.getAllRecentChats()

    suspend fun addRecentChatDbCache(recentChat: ContactModel) {
        local.addRecentChat(recentChat)
        cachedRecentChats[recentChat.contactUrl] = recentChat
    }

    suspend fun addAllRecentChatDbCache(recentChats: List<ContactModel>) {
        local.addRecentChats(recentChats)
        val chats = recentChats.associateBy { it.contactUrl }
        cachedRecentChats = HashMap(chats)
    }

    suspend fun updateRecentChatDbCache(recentChat: ContactModel) {
        local.updateRecentChat(recentChat)
        cachedRecentChats[recentChat.contactUrl] = recentChat
    }

    suspend fun deleteRecentChatDbCache(recentChatUrl: String) {
        val recentChat = getRecentChatFromCacheOrDb(recentChatUrl)
        if (recentChat != null) local.deleteRecentChat(recentChat)
        cachedRecentChats.remove(recentChatUrl)
    }

    suspend fun muteRecentChatDbCache(recentChatUrl: String) {
        val recentChat = getRecentChatFromCacheOrDb(recentChatUrl)
        if (recentChat != null) local.muteRecentChat(recentChat)
        cachedRecentChats[recentChatUrl]?.isMuted = true
    }

    suspend fun unMuteRecentChatDbCache(recentChatUrl: String) {
        val recentChat = getRecentChatFromCacheOrDb(recentChatUrl)
        if (recentChat != null) local.muteRecentChat(recentChat)
        cachedRecentChats[recentChatUrl]?.isMuted = false
    }

    suspend fun pinRecentChatDbCache(recentChat: ContactModel) {
        local.pinRecentChat(recentChat)
        cachedRecentChats[recentChat.contactUrl]?.isPinned = true
    }

    suspend fun unpinRecentDbCache(recentChat: ContactModel) {
        local.unpinRecentChat(recentChat)
        cachedRecentChats[recentChat.contactUrl]?.isPinned = false
    }

    suspend fun deleteAllRecentChatsDbCache() {
        local.deleteAllRecentChat()
        cachedRecentChats = hashMapOf()
    }

    suspend fun getRecentChatFromCacheOrDb(recentChatUrl: String): ContactModel? =
        cachedRecentChats[recentChatUrl] ?: local.getRecentChat(recentChatUrl)
}