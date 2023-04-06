package com.example.whisper.ui.recentchats

import com.example.whisper.vo.recentchats.RecentChatUiModel

interface RecentChatPresenter {

    fun onRecentChatClicked(chatId: String)

    fun onRecentChatLongClicked(contact: RecentChatUiModel): Boolean
}