package com.example.whisper.ui.utils.dialogs.contacts

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.domain.contact.*
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.ui.contacts.getContactState
import com.example.whisper.utils.common.CONTACT_BOTTOM_DIALOG_KEY
import com.example.whisper.utils.common.IS_RECENT_CHAT
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
    private val recentChatsRepository: RecentChatsRepository,
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
    private lateinit var contact: ContactModel

    init {
        viewModelScope.launch {
            with(savedStateHandle) {
                val contactModel = get<ContactModel>(CONTACT_BOTTOM_DIALOG_KEY)
                val isRecentChat = get<Boolean>(IS_RECENT_CHAT) ?: true

                contactModel?.apply {
                    _uiState.emit(
                        ContactBottomSheetDialogUiModel(
                            id = contactId,
                            channelUrl = contactUrl,
                            profileImageUrl = picture,
                            username = username,
                            email = email,
                            isMuted = isMuted,
                            isPinned = isPinned,
                            contactStatus = getContactState(memberState),
                            isRecentChat = isRecentChat
                        )
                    )
                    contact = contactModel
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
                UnpinContactUseCase(contactsRepository, recentChatsRepository).invoke(
                    contact = contact
                ) {
                    when (it) {
                        is UnpinContactsState.ErrorState -> {
                            showErrorState()
                        }
                        is UnpinContactsState.SuccessState -> {
                            _dismissDialog.emit(true)
                        }
                    }
                }
            } else {
                PinContactUseCase(contactsRepository, recentChatsRepository).invoke(
                    contact = contact
                ) {
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

            BlockContactUseCase(contactsRepository, recentChatsRepository).invoke(
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

            DeleteContactUseCase(contactsRepository, recentChatsRepository).invoke(
                contactUrl = _uiState.value.channelUrl,
                coroutineScope = viewModelScope,
                _uiState.value.isRecentChat
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
        UnmuteContactUseCase(contactsRepository, recentChatsRepository).invoke(
            contactUrl = _uiState.value.channelUrl,
        ) {
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

    private suspend fun mute() {
        MuteContactUseCase(contactsRepository, recentChatsRepository).invoke(
            contactUrl = _uiState.value.channelUrl,
        ) {
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