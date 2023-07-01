package com.example.whisper.ui.signup

import android.Manifest
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.signup.SignUpState
import com.example.whisper.domain.signup.SignUpUseCase
import com.example.whisper.domain.signup.ValidateEmailUseCase
import com.example.whisper.domain.signup.ValidationStates
import com.example.whisper.domain.user.UpdateUserState
import com.example.whisper.domain.user.UpdateUserUseChase
import com.example.whisper.navigation.GalleryNavigation
import com.example.whisper.navigation.NavGraph
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.base.BaseInputChangeViewModel
import com.example.whisper.utils.FileUtils
import com.example.whisper.utils.NetworkHandler
import com.example.whisper.utils.common.INVALID_RES
import com.example.whisper.utils.permissions.*
import com.example.whisper.vo.dialogs.TitleMessageDialog
import com.example.whisper.vo.signup.SignUpUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val networkHandler: NetworkHandler,
    private val permissionChecker: PermissionChecker,
    private val fileUtils: FileUtils
) : BaseInputChangeViewModel(), SignUpPresenter, PermissionStateHandler {

    val uiState
        get() = _uiState.asStateFlow()

    val permissionState
        get() = _permissionState.asSharedFlow()

    private val _uiState = MutableStateFlow(SignUpUiModel())
    private val _permissionState = MutableSharedFlow<Permission>()

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun setProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    pictureFile = fileUtils.createFile(uri),
                    profilePicture = uri
                )
            )
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onContinueClick() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))

            val signUpResult = SignUpUseCase(
                userRepository,
                contactsRepository,
                recentChatsRepository,
                networkHandler
            ).invoke(_uiState.value.email, _uiState.value.password)

            when (signUpResult) {
                is SignUpState.CredentialsErrorState -> showValidationErrorDialog()
                is SignUpState.ErrorState -> showErrorDialog()
                is SignUpState.NetworkErrorState -> showNoNetworkErrorDialog()
                is SignUpState.SuccessState -> {
                    _uiState.emit(_uiState.value.copy(isLoading = false))
                    navigateToStepTwo()
                }
            }
        }
    }

    override fun onFinish() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = UpdateUserUseChase(userRepository, networkHandler).invoke(
                _uiState.value.username,
                _uiState.value.pictureFile
            )

            when (state) {
                UpdateUserState.ErrorState -> showErrorDialog()
                UpdateUserState.NetworkErrorState -> showNoNetworkErrorDialog()
                UpdateUserState.SuccessState -> navigateToRecentChats()
            }
        }
    }

    override fun onBackClick() {
        viewModelScope.launch {
            _navigationFlow.emit(PopBackStack)
        }
    }

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
            setIsPasswordValid(password.toString())
            setIsConfirmPasswordValid(_uiState.value.confirmPassword)
            setContinueButtonVisibility()
            _uiState.emit(_uiState.value.copy(password = password.toString()))
        }
    }

    override fun onConfirmPasswordTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { confirmPassword ->
            setIsConfirmPasswordValid(confirmPassword.toString())
            setIsPasswordValid(_uiState.value.password)
            setContinueButtonVisibility()
            _uiState.emit(_uiState.value.copy(confirmPassword = confirmPassword.toString()))
        }
    }

    override fun onUsernameTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { username ->
            setIsUsernameValid(username.toString())
            _uiState.emit(_uiState.value.copy(username = username.toString()))
        }
    }

    override fun onProfileImageClick() {
        viewModelScope.launch {
            if (permissionChecker.isPermissionGranted()) {
                _navigationFlow.emit(GalleryNavigation)
            } else {
                _permissionState.emit(
                    PermissionRequest(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                )
            }
        }
    }

    override fun onPermissionState(state: PermissionState) {
        viewModelScope.launch {
            when (state) {
                is GrantedState -> {
                    _navigationFlow.emit(GalleryNavigation)
                }
                is DeniedState -> {
                    val dialog = TitleMessageDialog(
                        title = R.string.dialog_external_storage_denied_title,
                        message = R.string.dialog_external_storage_denied_body
                    )

                    _dialogFlow.emit(dialog)
                }
                else -> {
                    // Rationale State
                    val dialog = TitleMessageDialog(
                        title = R.string.dialog_external_storage_grant_permission_title,
                        message = R.string.dialog_external_storage_grant_permission_body,
                        positiveButtonClickListener = {
                            viewModelScope.launch {
                                _permissionState.emit(
                                    PermissionRequest(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                                )
                            }
                        }
                    )
                    _dialogFlow.emit(dialog)
                }
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun setIsPasswordValid(password: String) {
        when (password.length) {
            in 1 until PASSWORD_MIN_LENGTH -> {
                _uiState.emit(
                    _uiState.value.copy(
                        passwordError = R.string.invalid_password,
                        passwordErrorEnabled = true
                    )
                )
            }
            else -> {
                _uiState.emit(
                    _uiState.value.copy(
                        passwordError = INVALID_RES,
                        passwordErrorEnabled = false
                    )
                )
            }
        }
    }

    private suspend fun setIsConfirmPasswordValid(confirmPassword: String) {
        _uiState.value.apply {
            if (confirmPassword.contentEquals(password).not()) {
                _uiState.emit(
                    _uiState.value.copy(
                        confirmPasswordError = R.string.invalid_confirm_password,
                        confirmPasswordErrorEnabled = true
                    )
                )
            } else {
                _uiState.emit(
                    _uiState.value.copy(
                        confirmPasswordError = INVALID_RES,
                        confirmPasswordErrorEnabled = false
                    )
                )
            }
        }
    }

    private suspend fun setIsUsernameValid(username: String) {
        when {
            username.length <= USERNAME_MIN_LENGTH -> {
                _uiState.emit(
                    _uiState.value.copy(
                        usernameError = R.string.invalid_username,
                        usernameErrorEnabled = true,
                        isFinishEnabled = false
                    )
                )
            }
            else -> {
                _uiState.emit(
                    _uiState.value.copy(
                        usernameError = INVALID_RES,
                        usernameErrorEnabled = false,
                        isFinishEnabled = true
                    )
                )
            }
        }
    }

    private suspend fun setContinueButtonVisibility() {
        _uiState.emit(_uiState.value.copy(isContinueEnabled = _uiState.value.enableContinueButton()))
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

    private suspend fun showErrorDialog() {
        _uiState.emit(_uiState.value.copy(isLoading = false))
        _dialogFlow.emit(
            TitleMessageDialog(
                R.string.error_dialog_title,
                R.string.error_something_went_wrong_try_again
            )
        )
    }

    private suspend fun navigateToRecentChats() {
        _navigationFlow.emit(NavGraph(R.id.action_signUpStepTwoFragment_to_baseContactsFragment))
    }

    private suspend fun navigateToStepTwo() {
        _navigationFlow.emit(NavGraph(R.id.action_signUpFragment_to_signUpStepTwoFragment))
    }
}