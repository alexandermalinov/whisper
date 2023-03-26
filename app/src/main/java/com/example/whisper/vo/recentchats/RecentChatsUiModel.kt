package com.example.whisper.vo.recentchats

data class RecentChatsUiModel(
    val uiState: RecentChatState = RecentChatState.IDLE
)

enum class RecentChatState {
    IDLE,
    LOADING,
    ERROR,
    EMPTY
}