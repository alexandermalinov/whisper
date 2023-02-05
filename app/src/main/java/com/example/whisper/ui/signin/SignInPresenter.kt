package com.example.whisper.ui.signin

import kotlinx.coroutines.flow.Flow

interface SignInPresenter {

    fun onEmailTextChanged(textFlow: Flow<CharSequence>)

    fun onPasswordTextChanged(textFlow: Flow<CharSequence>)

    fun onContinueClick()

    fun onBackClick()
}