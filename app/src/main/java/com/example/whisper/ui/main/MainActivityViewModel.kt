package com.example.whisper.ui.main

import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val userRepository: UserRepository) :
    BaseViewModel() {


}