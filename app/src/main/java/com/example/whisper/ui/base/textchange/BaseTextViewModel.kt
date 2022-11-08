package com.example.whisper.ui.base.textchange

import androidx.lifecycle.viewModelScope
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.utils.common.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseTextViewModel : BaseViewModel() {

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
}