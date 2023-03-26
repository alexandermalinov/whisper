package com.example.whisper.ui.base

import androidx.lifecycle.ViewModel
import com.example.whisper.ui.utils.menu.MenuUiModel
import com.example.whisper.navigation.Destination
import com.example.whisper.vo.dialogs.Dialog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

open class BaseViewModel : ViewModel() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    val navigationFlow
        get() = _navigationFlow.asSharedFlow()

    val popupMenuFlow
        get() = _popupMenuFlow.asSharedFlow()

    val dialogFlow
        get() = _dialogFlow.asSharedFlow()

    protected val _navigationFlow = MutableSharedFlow<Destination>()
    protected val _popupMenuFlow = MutableSharedFlow<MenuUiModel>()
    protected val _dialogFlow = MutableSharedFlow<Dialog>()
}