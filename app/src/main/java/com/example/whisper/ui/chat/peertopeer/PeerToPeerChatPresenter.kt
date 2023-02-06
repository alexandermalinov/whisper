package com.example.whisper.ui.chat.peertopeer

interface PeerToPeerChatPresenter {

    fun onPlayClicked()

    fun onPauseClicked()

    fun onRecord(): Boolean = true
}