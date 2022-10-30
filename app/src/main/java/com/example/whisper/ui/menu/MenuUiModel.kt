package com.connection.menu

import androidx.annotation.MenuRes
import com.example.whisper.utils.common.INVALID_RES

sealed class MenuUiModel

data class PopupMenuUiModel(
    @MenuRes
    val layout: Int = INVALID_RES,
    val onClickListener: ((Int) -> Unit)? = null
) : MenuUiModel()