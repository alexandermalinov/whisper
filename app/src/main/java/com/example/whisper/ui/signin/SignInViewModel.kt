package com.example.whisper.ui.signin

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.signup.ValidateEmailUseCase
import com.example.whisper.domain.signup.ValidationStates
import com.example.whisper.navigation.NavGraph
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.base.BaseInputChangeViewModel
import com.example.whisper.utils.common.INVALID_RES
import com.example.whisper.utils.isNetworkAvailable
import com.example.whisper.vo.dialogs.TitleMessageDialog
import com.example.whisper.vo.signin.SignInUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val application: Application
) : BaseInputChangeViewModel(), SignInPresenter {

    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(SignInUiModel())

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onEmailTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { email ->
            when (validateEmailUseCase.invoke(email.toString())) {
                ValidationStates.VALID -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            email = email.toString(),
                            emailErrorEnabled = false,
                            emailError = INVALID_RES,
                            emailEndIcon = R.drawable.ic_checkbox
                        )
                    )
                }
                ValidationStates.INVALID -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            email = email.toString(),
                            emailErrorEnabled = true,
                            emailError = R.string.invalid_email,
                            emailEndIcon = null
                        )
                    )
                }
                ValidationStates.EMPTY -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            email = email.toString(),
                            emailErrorEnabled = false,
                            emailError = INVALID_RES,
                            emailEndIcon = null
                        )
                    )
                }
            }
            setContinueButtonVisibility()
        }
    }

    override fun onPasswordTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { password ->
            when {
                password.length < PASSWORD_MIN_LENGTH -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            password = password.toString(),
                            passwordError = R.string.invalid_password,
                            passwordErrorEnabled = true
                        )
                    )
                }
                else -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            password = password.toString(),
                            passwordError = INVALID_RES,
                            passwordErrorEnabled = false
                        )
                    )
                }
            }
            setContinueButtonVisibility()
        }
    }

    override fun onContinueClick() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            userRepository.loginFirebaseAuth(_uiState.value.email, _uiState.value.password) {
                viewModelScope.launch {
                    it.foldSuspend(
                        { onFailure ->
                            if (application.isNetworkAvailable())
                                showValidationErrorDialog()
                            else
                                showNoNetworkErrorDialog()
                        },
                        { onSuccess ->
                            userRepository.setIsSignedIn(true)
                            navigateToRecentChats()
                        }
                    )
                }
            }
        }
    }

    override fun onBackClick() {
        viewModelScope.launch {
            _navigationFlow.emit(PopBackStack)
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun setContinueButtonVisibility() {
        _uiState.value.apply {
            _uiState.emit(
                copy(
                    isContinueEnabled = emailError == INVALID_RES && password.length >= PASSWORD_MIN_LENGTH
                )
            )
        }
    }

    private fun navigateToRecentChats() {
        viewModelScope.launch {
            _navigationFlow.emit(NavGraph(R.id.action_signInFragment_to_baseContactsFragment))
        }
    }

    private suspend fun showValidationErrorDialog() {
        _uiState.emit(_uiState.value.copy(isLoading = false))
        _dialogFlow.emit(
            TitleMessageDialog(
                R.string.error_dialog_title_try_again,
                R.string.error_dialog_message_body_invalid_credentials
            )
        )
    }

    private suspend fun showNoNetworkErrorDialog() {
        _uiState.emit(_uiState.value.copy(isLoading = false))
        _dialogFlow.emit(
            TitleMessageDialog(
                R.string.error_dialog_title_network,
                R.string.error_dialog_message_body_no_network
            )
        )
    }
}