package com.example.whisper.ui.base

import androidx.lifecycle.viewModelScope
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.utils.common.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseInputChangeViewModel : BaseViewModel() {

    /* --------------------------------------------------------------------------------------------
     * Protected
    ---------------------------------------------------------------------------------------------*/
    protected var lastText = EMPTY

    fun onTextChanged(
        searchFlow: Flow<CharSequence>,
        delay: Long = 1000,
        fetchData: suspend (CharSequence) -> Unit
    ) {
        searchFlow
            .debounce(delay)
            .onEach { fetchData(it) }
            .launchIn(viewModelScope)
    }

    companion object {
        const val PASSWORD_MIN_LENGTH = 8
        const val USERNAME_MIN_LENGTH = 4
    }
}