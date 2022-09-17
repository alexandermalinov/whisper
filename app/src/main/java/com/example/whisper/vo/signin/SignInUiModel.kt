package com.example.whisper.vo.signin

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.whisper.utils.common.EMPTY

data class SignInUiModel(
    val email: String = EMPTY,
    val password: String = EMPTY,
) : BaseObservable() {

    /*@get:Bindable
    var loading: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.loading)
        }

    @get:Bindable
    var emailError: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.emailError)
        }

    @get:Bindable
    var signInError: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.signInError)
        }*/
}
