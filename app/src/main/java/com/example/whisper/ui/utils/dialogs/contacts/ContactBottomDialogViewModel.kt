package com.example.whisper.ui.utils.dialogs.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.utils.common.*
import com.example.whisper.vo.dialogs.contacts.ContactBottomSheetDialogUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ContactBottomDialogViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(), ContactBottomDialogPresenter {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    val uiState
        get() = _uiState.asStateFlow()

    val dismissDialog
        get() = _dismissDialog.asSharedFlow()

    private val _uiState = MutableStateFlow(ContactBottomSheetDialogUiModel())
    private val _dismissDialog = MutableSharedFlow<Boolean>()

    init {
        viewModelScope.launch {
            val channelUrl = savedStateHandle.get<String>(CHANNEL_URL)
            val contactId = savedStateHandle.get<String>(CONTACT_ID)
            val contactProfileImage = savedStateHandle.get<String>(CONTACT_PROFILE_IMAGE) ?: EMPTY
            val contactUsername = savedStateHandle.get<String>(CONTACT_USERNAME) ?: EMPTY
            val contactEmail = savedStateHandle.get<String>(CONTACT_EMAIL) ?: EMPTY

            if (channelUrl != null && contactId != null) {
                _uiState.emit(
                    ContactBottomSheetDialogUiModel(
                        id = contactId,
                        channelUrl = channelUrl,
                        profileImageUrl = contactProfileImage,
                        username = contactUsername,
                        email = contactEmail
                    )
                )
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun pinContact() {
        // TODO("Not yet implemented")
    }

    override fun muteContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            delay(CONTACT_ACTION_DELAY_TIME)

            contactsRepository.muteContact(_uiState.value.channelUrl, _uiState.value.id) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        _uiState.emit(_uiState.value.copy(isLoading = false))
                    }, {
                        _dismissDialog.emit(true)
                    })
                }
            }
        }
    }

    override fun unmuteContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            delay(CONTACT_ACTION_DELAY_TIME)

            contactsRepository.unmuteContact(
                _uiState.value.channelUrl,
                _uiState.value.id
            ) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        _uiState.emit(_uiState.value.copy(isLoading = false))
                    }, {
                        _dismissDialog.emit(true)
                    })
                }
            }
        }
    }

    override fun blockContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            delay(CONTACT_ACTION_DELAY_TIME)

            contactsRepository.blockContact(_uiState.value.id) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        _uiState.emit(_uiState.value.copy(isLoading = false))
                    }, {
                        _dismissDialog.emit(true)
                    })
                }
            }
        }
    }

    override fun unblockContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            delay(CONTACT_ACTION_DELAY_TIME)

            contactsRepository.blockContact(_uiState.value.id) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        _uiState.emit(_uiState.value.copy(isLoading = false))
                    }, {
                        _dismissDialog.emit(true)
                    })
                }
            }
        }
    }

    override fun deleteContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            delay(CONTACT_ACTION_DELAY_TIME)

            contactsRepository.deleteContact(_uiState.value.channelUrl) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        _uiState.emit(_uiState.value.copy(isLoading = false))
                    }, {
                        _dismissDialog.emit(true)
                    })
                }
            }
        }
    }

/* --------------------------------------------------------------------------------------------
 * Private
---------------------------------------------------------------------------------------------*/

    companion object {
        private const val CONTACT_ACTION_DELAY_TIME = 2000L
    }
}