package com.example.whisper.vo.dialogs

import androidx.annotation.StringRes
import com.example.whisper.R
import com.example.whisper.utils.common.INVALID_RES

/**
 * Base dialog class that can be inherited by all types of dialogs
 */
sealed class Dialog

/**
 * Alert dialog with title, message and two optional buttons
 */
open class TitleMessageDialog(
    @StringRes val title: Int = INVALID_RES,
    @StringRes val message: Int = INVALID_RES,
    @StringRes val positiveButtonText: Int = R.string.ok,
    @StringRes val negativeButtonText: Int? = null,
    val positiveButtonClickListener: (() -> Unit)? = null,
    val negativeButtonClickListener: (() -> Unit)? = null,
    val cancelable: Boolean = true
) : Dialog()