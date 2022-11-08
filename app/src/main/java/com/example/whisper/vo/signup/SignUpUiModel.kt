package com.example.whisper.vo.signup

import android.net.Uri
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.whisper.BR
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.INVALID_RES

data class SignUpUiModel(
    var email: String = EMPTY,
    var username: String = EMPTY,
    var password: String = EMPTY,
    var confirmPassword: String = EMPTY,
    val isLoading: Boolean = false
) : BaseObservable() {

    @get:Bindable
    var profilePicture: Uri? = Uri.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.profilePicture)
        }

    @get:Bindable
    var emailErrorEnabled: Boolean = false
        set(value) {
            field = value
            setContinueEnabled()
            notifyPropertyChanged(BR.emailErrorEnabled)
        }

    @get:Bindable
    var emailError: Int = INVALID_RES
        set(value) {
            field = value
            notifyPropertyChanged(BR.emailError)
        }

    @get:Bindable
    var emailEndIcon: Int? = INVALID_RES
        set(value) {
            field = value
            notifyPropertyChanged(BR.emailEndIcon)
        }

    @get:Bindable
    var passwordError: Int = INVALID_RES
        set(value) {
            field = value
            notifyPropertyChanged(BR.passwordError)
        }

    @get:Bindable
    var passwordErrorEnabled: Boolean = false
        set(value) {
            field = value
            setContinueEnabled()
            notifyPropertyChanged(BR.passwordErrorEnabled)
        }

    @get:Bindable
    var confirmPasswordError: Int = INVALID_RES
        set(value) {
            field = value
            notifyPropertyChanged(BR.confirmPasswordError)
        }

    @get:Bindable
    var confirmPasswordErrorEnabled: Boolean = false
        set(value) {
            field = value
            setContinueEnabled()
            notifyPropertyChanged(BR.confirmPasswordErrorEnabled)
        }

    @get:Bindable
    var isContinueEnabled: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.continueEnabled)
        }

    private fun setContinueEnabled() {
        isContinueEnabled = emailErrorEnabled.not() &&
                passwordErrorEnabled.not() &&
                confirmPasswordErrorEnabled.not() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank()
    }
}
