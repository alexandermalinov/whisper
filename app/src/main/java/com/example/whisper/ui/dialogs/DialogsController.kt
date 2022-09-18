package com.example.whisper.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import com.example.whisper.R
import com.example.whisper.utils.common.INVALID_RES
import com.example.whisper.vo.dialogs.Dialog
import com.example.whisper.vo.dialogs.TitleMessageDialog

fun Context.showDialog(dialog: Dialog) {
    when (dialog) {
        is TitleMessageDialog -> showTitleMessageDialog(
            dialog.title,
            dialog.message,
            dialog.positiveButtonText,
            dialog.negativeButtonText,
            { it, _ ->
                dialog.positiveButtonClickListener?.invoke()
                it.dismiss()
            },
            { it, _ ->
                dialog.negativeButtonClickListener?.invoke()
                it.dismiss()
            },
            dialog.cancelable
        )
    }
}

private fun Context.showTitleMessageDialog(
    @StringRes title: Int = INVALID_RES,
    @StringRes message: Int = INVALID_RES,
    @StringRes positiveButtonText: Int = R.string.ok,
    @StringRes negativeButtonText: Int = R.string.cancel,
    positiveButtonClickListener: DialogInterface.OnClickListener? = null,
    negativeButtonClickListener: DialogInterface.OnClickListener? = null,
    cancelable: Boolean = true
): AlertDialog = AlertDialog.Builder(this).apply {
    if (title != INVALID_RES) setTitle(title)
    if (message != INVALID_RES) setMessage(message)
    if (positiveButtonText != INVALID_RES)
        setPositiveButton(positiveButtonText, positiveButtonClickListener)
    if (negativeButtonText != INVALID_RES)
        setNegativeButton(negativeButtonText, negativeButtonClickListener)
    setCancelable(cancelable)
    create()
}.show()