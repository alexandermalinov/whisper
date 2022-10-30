package com.example.whisper.vo.signup

import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.INVALID_RES

data class SignUpUiModel(
    val email: String = EMPTY,
    val emailError: Int = INVALID_RES,
    val username: String = EMPTY,
    val usernameError: Int = INVALID_RES,
    val password: String = EMPTY,
    val passwordError: Int = INVALID_RES,
    val confirmPassword: String = EMPTY,
    val confirmPasswordError: Int = INVALID_RES,
    val isLoading: Boolean = false
)
