package com.example.whisper.ui.contacts

import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseChatViewModel
import com.example.whisper.vo.contacts.ContactsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseChatViewModel(userRepository, contactsRepository), ContactPresenter {

    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(ContactsUiState())

    override fun navigateToAddContact() {
        _navigationLiveData.value = NavGraph(R.id.action_contactsFragment_to_addContactFragment)
    }
}