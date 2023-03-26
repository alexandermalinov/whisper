package com.example.whisper.vo.addcontact

data class AddContactUiModel(
    val state: AddContactUiState = AddContactUiState.SEARCH_FOUND
)

enum class AddContactUiState {
    LOADING,
    SEARCH_EMPTY,
    SEARCH_FOUND,
    ERROR
}
