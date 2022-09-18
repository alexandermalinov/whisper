package com.example.whisper.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.connection.menu.MenuUiModel
import com.example.whisper.navigation.Destination
import com.example.whisper.utils.SingleLiveEvent
import com.example.whisper.vo.dialogs.Dialog

open class BaseViewModel : ViewModel() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    val navigationLiveData: LiveData<Destination>
        get() = _navigationLiveData

    val popupMenuLiveData: LiveData<MenuUiModel>
        get() = _popupMenuLiveData

    val dialogLiveData: LiveData<Dialog>
        get() = _dialogLiveData

    protected val _navigationLiveData = SingleLiveEvent<Destination>()
    protected val _popupMenuLiveData = SingleLiveEvent<MenuUiModel>()
    protected val _dialogLiveData = SingleLiveEvent<Dialog>()
}