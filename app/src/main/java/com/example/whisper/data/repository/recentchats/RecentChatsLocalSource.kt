package com.example.whisper.data.repository.recentchats

import com.example.whisper.data.local.dao.RecentChatDao
import com.example.whisper.data.local.entity.toRecentChatModel
import com.example.whisper.data.local.entity.toRecentChatModels
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.toRecentChat
import com.example.whisper.data.local.model.toRecentChats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecentChatsLocalSource @Inject constructor(
    private val recentChatDao: RecentChatDao
) : RecentChatsRepository.LocalSource {

    override suspend fun updateRecentChat(recentChat: ContactModel) {
        recentChatDao.updateRecentChat(recentChat.toRecentChat())
    }

    override fun getRecentChatsFlow(): Flow<List<ContactModel>> =
        recentChatDao.getRecentChatsFlow().map { it.toRecentChatModels() }

    override suspend fun getAllRecentChats(): List<ContactModel> =
        recentChatDao.getRecentChats().toRecentChatModels()

    override suspend fun addRecentChat(recentChat: ContactModel) {
        recentChatDao.insertRecentChat(recentChat.toRecentChat())
    }

    override suspend fun addRecentChats(recentChats: List<ContactModel>) {
        recentChatDao.deleteAllRecentChats()
        recentChatDao.insertRecentChats(recentChats.toRecentChats())
    }

    override suspend fun getRecentChat(recentChatUrl: String): ContactModel? =
        recentChatDao.getRecentChat(recentChatUrl)?.toRecentChatModel()

    override suspend fun deleteRecentChat(recentChat: ContactModel) {
        recentChatDao.deleteRecentChat(recentChat.toRecentChat())
    }

    override suspend fun deleteAllRecentChat() {
        recentChatDao.deleteAllRecentChats()
    }

    override suspend fun muteRecentChat(recentChat: ContactModel) {
        val mutedRecentChat = recentChat.copy(isMuted = true).toRecentChat()
        recentChatDao.updateRecentChat(mutedRecentChat)
    }

    override suspend fun unmuteRecentChat(recentChat: ContactModel) {
        val unmutedRecentChat = recentChat.copy(isMuted = false).toRecentChat()
        recentChatDao.updateRecentChat(unmutedRecentChat)
    }

    override suspend fun pinRecentChat(recentChat: ContactModel) {
        val pinnedRecentChat = recentChat.copy(isPinned = true).toRecentChat()
        recentChatDao.updateRecentChat(pinnedRecentChat)
    }

    override suspend fun unpinRecentChat(recentChat: ContactModel) {
        val pinnedRecentChat = recentChat.copy(isPinned = false).toRecentChat()
        recentChatDao.updateRecentChat(pinnedRecentChat)
    }
}