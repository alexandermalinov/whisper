package com.example.whisper.ui.utils.dialogs.contacts

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.*
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.ui.contacts.ContactState
import com.example.whisper.utils.common.*
import com.example.whisper.utils.isNetworkAvailable
import com.example.whisper.vo.dialogs.contacts.ContactBottomSheetDialogUiModel
import com.example.whisper.vo.dialogs.contacts.ContactBottomSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactBottomDialogViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val userRepository: UserRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle,
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
            with(savedStateHandle) {
                val channelUrl = get<String>(CHANNEL_URL)
                val contactId = get<String>(CONTACT_ID)
                val contactProfileImage = get<String>(CONTACT_PROFILE_IMAGE) ?: EMPTY
                val contactUsername = get<String>(CONTACT_USERNAME) ?: EMPTY
                val contactEmail = get<String>(CONTACT_EMAIL) ?: EMPTY
                val contactIsMuted = get<Boolean>(CONTACT_IS_MUTED) ?: false
                val contactIsPinned = get<Boolean>(CONTACT_IS_PINNED) ?: false
                val contactStatus = get<ContactState>(CONTACT_STATUS) ?: ContactState.JOINED

                if (channelUrl != null && contactId != null) {
                    _uiState.emit(
                        ContactBottomSheetDialogUiModel(
                            id = contactId,
                            channelUrl = channelUrl,
                            profileImageUrl = contactProfileImage,
                            username = contactUsername,
                            email = contactEmail,
                            isMuted = contactIsMuted,
                            isPinned = contactIsPinned,
                            contactStatus = contactStatus
                        )
                    )
                }
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun pinContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactBottomSheetState.LOADING))
            delay(CONTACT_ACTION_DELAY_TIME)

            if (_uiState.value.isPinned) {
                UnpinContactUseCase(contactsRepository).invoke(
                    contactId = _uiState.value.id,
                    contactUrl = _uiState.value.channelUrl,
                    coroutineScope = viewModelScope
                ) {
                    viewModelScope.launch {
                        when (it) {
                            is UnpinContactsState.ErrorState -> {
                                showErrorState()
                            }
                            is UnpinContactsState.SuccessState -> {
                                _dismissDialog.emit(true)
                            }
                        }
                    }
                }
            } else {
                PinContactUseCase(contactsRepository).invoke(
                    contactId = _uiState.value.id,
                    contactUrl = _uiState.value.channelUrl,
                    coroutineScope = viewModelScope
                ) {
                    viewModelScope.launch {
                        when (it) {
                            is PinContactsState.ErrorState -> {
                                showErrorState()
                            }
                            is PinContactsState.SuccessState -> {
                                _dismissDialog.emit(true)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun muteOrUnmuteContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactBottomSheetState.LOADING))
            delay(CONTACT_ACTION_DELAY_TIME)

            if (_uiState.value.isMuted) unMute() else mute()
        }
    }

    override fun blockContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactBottomSheetState.LOADING))
            delay(CONTACT_ACTION_DELAY_TIME)

            BlockContactUseCase(contactsRepository).invoke(
                contactUrl = _uiState.value.channelUrl,
                coroutineScope = viewModelScope
            ) {
                viewModelScope.launch {
                    when (it) {
                        is BlockContactState.ErrorState -> {
                            showErrorState()
                        }
                        is BlockContactState.SuccessState -> {
                            _dismissDialog.emit(true)
                        }
                    }
                }
            }
        }
    }

    override fun deleteContact() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactBottomSheetState.LOADING))
            delay(CONTACT_ACTION_DELAY_TIME)

            DeleteContactUseCase(contactsRepository).invoke(
                contactUrl = _uiState.value.channelUrl,
                coroutineScope = viewModelScope
            ) {
                viewModelScope.launch {
                    when (it) {
                        is DeleteContactState.ErrorState -> {
                            showErrorState()
                        }
                        is DeleteContactState.SuccessState -> {
                            _dismissDialog.emit(true)
                        }
                    }
                }
            }
        }
    }

    override fun tryAgain() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(uiState = ContactBottomSheetState.IDLE))
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ----------------------------------------------------------------------------------------------*/
    private suspend fun unMute() {
        UnmuteContactUseCase(contactsRepository).invoke(
            contactId = _uiState.value.id,
            contactUrl = _uiState.value.channelUrl,
            coroutineScope = viewModelScope
        ) {
            viewModelScope.launch {
                when (it) {
                    is UnmuteContactState.ErrorState -> {
                        showErrorState()
                    }
                    is UnmuteContactState.SuccessState -> {
                        _dismissDialog.emit(true)
                    }
                }
            }
        }
    }

    private suspend fun mute() {
        MuteContactUseCase(contactsRepository).invoke(
            contactId = _uiState.value.id,
            contactUrl = _uiState.value.channelUrl,
            coroutineScope = viewModelScope
        ) {
            viewModelScope.launch {
                when (it) {
                    is MuteContactState.ErrorState -> {
                        showErrorState()
                    }
                    is MuteContactState.SuccessState -> {
                        _dismissDialog.emit(true)
                    }
                }
            }
        }
    }

    private suspend fun showErrorState() {
        when {
            application.isNetworkAvailable().not() -> _uiState.emit(
                _uiState.value.copy(
                    uiState = ContactBottomSheetState.ERROR,
                    errorTitle = R.string.error_network_message,
                    errorMessage = R.string.error_network
                )
            )
            else -> _uiState.emit(
                _uiState.value.copy(
                    uiState = ContactBottomSheetState.ERROR,
                    errorTitle = R.string.error_title_oops,
                    errorMessage = R.string.error_something_went_wrong_try_again
                )
            )
        }
    }

    companion object {
        private const val CONTACT_ACTION_DELAY_TIME = 2000L
    }
}