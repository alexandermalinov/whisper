package com.example.whisper.ui.chat.peertopeer

import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType

interface PeerToPeerChatPresenter {

    fun onBackClicked()

    fun sendMessage(messageType: MessageUiType? = null)

    fun onChooseGalleryImageClicked()

    fun onChooseDocument()

    fun onPlayClicked()

    fun onPauseClicked()

    fun onRecord(): Boolean = true
}