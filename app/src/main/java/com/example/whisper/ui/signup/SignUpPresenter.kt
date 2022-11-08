package com.example.whisper.ui.signup

import kotlinx.coroutines.flow.Flow

interface SignUpPresenter {

    fun onContinueClick()

    fun onBackClick()

    fun onEmailTextChanged(textFlow: Flow<CharSequence>)

    fun onPasswordTextChanged(textFlow: Flow<CharSequence>)

    fun onConfirmPasswordTextChanged(textFlow: Flow<CharSequence>)

    fun onFinish()

    fun onProfileImageClick()
}