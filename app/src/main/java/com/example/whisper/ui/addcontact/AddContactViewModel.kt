package com.example.whisper.ui.addcontact

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseChatViewModel
import com.example.whisper.vo.addcontact.AddContactUiModel
import com.example.whisper.vo.contacts.toContactsUiModel
import com.example.whisper.vo.dialogs.TitleMessageDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseChatViewModel(userRepository, contactsRepository), AddContactPresenter {

    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(AddContactUiModel())

    init {
        viewModelScope.launch {
            contactsRepository.getUserNotConnectedWith { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        _dialogLiveData.value = TitleMessageDialog(
                            R.string.error_dialog_title_try_again,
                            R.string.error_dialog_message_body_invalid_credentials
                        )
                    }, { users ->
                        _uiState.emit(_uiState.value.copy(contacts = users.toContactsUiModel()))
                    })
                }
            }
        }
    }

    override fun onUsernameTextChanged(textFlow: Flow<CharSequence>) {
        textFlow
            .debounce(1000L)
            .onEach {
                // TODO
            }
            .launchIn(viewModelScope)
    }

    override fun addContact(contactId: String) {
        _navigationLiveData.value =
            NavGraph(R.id.action_addContactFragment_to_peerToPeerChatFragment)
    }

}