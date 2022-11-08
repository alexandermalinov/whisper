package com.example.whisper.domain.signing

class ValidateEmailUseCase {

    operator fun invoke(email: String): ValidationStates = when {
        email.isBlank() -> ValidationStates.EMPTY
        email.matches(EMAIL_PATTERN.toRegex()) && email.isNotBlank() -> ValidationStates.VALID
        else -> ValidationStates.INVALID
    }

    companion object {
        private const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    }
}