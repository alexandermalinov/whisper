package com.example.whisper.ui.utils.dialogs.contacts

interface ContactBottomDialogPresenter {

    fun pinContact()

    fun muteOrUnmuteContact()

    fun blockContact()

    fun unblockContact()

    fun deleteContact()

    fun tryAgain()
}