package com.example.whisper.vo.signup

import android.net.Uri
import androidx.core.net.toUri
import com.example.whisper.data.local.entity.User
import com.example.whisper.data.remote.model.user.UserModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.INVALID_RES
import java.io.File

data class SignUpUiModel(
    val id: String = EMPTY,
    val email: String = EMPTY,
    val username: String = EMPTY,
    val password: String = EMPTY,
    val confirmPassword: String = EMPTY,
    val pictureFile: File = File(EMPTY),
    val profilePicture: Uri? = Uri.EMPTY,

    val emailErrorEnabled: Boolean = false,
    val emailError: Int = INVALID_RES,
    val emailEndIcon: Int? = INVALID_RES,

    val passwordError: Int = INVALID_RES,
    val passwordErrorEnabled: Boolean = false,

    val confirmPasswordError: Int = INVALID_RES,
    val confirmPasswordErrorEnabled: Boolean = false,

    val usernameError: Int = INVALID_RES,
    val usernameErrorEnabled: Boolean = false,

    val isContinueEnabled: Boolean = false,
    val isFinishEnabled: Boolean = false,
    val isLoading: Boolean = false,
) {
    fun enableContinueButton() = emailErrorEnabled.not() &&
            passwordErrorEnabled.not() &&
            confirmPasswordErrorEnabled.not() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank()
}

fun SignUpUiModel.toUserModel(id: String) = UserModel(
    id = id,
    email = email,
    password = password,
    username = username,
    profilePictureUrl = File(EMPTY)
)

fun SignUpUiModel.toUser(id: String) = User(
    id = id,
    userEmail = email,
    userPassword = password,
    username = username,
    userPicture = pictureFile.toUri().toString()
)