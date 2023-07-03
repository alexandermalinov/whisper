package com.example.whisper.ui.chat.peertopeer

interface MessagesPresenter {

    fun onPlayVideoClicked(id: String)

    fun onPauseVideoClicked(id: String)

    fun openFile(fileUrl: String, filePath: String)
}