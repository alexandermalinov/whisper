package com.example.whisper.ui.utils.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.whisper.ui.utils.dialogs.contacts.ContactBottomDialogFragment
import com.example.whisper.R
import com.example.whisper.utils.common.INVALID_RES
import com.example.whisper.vo.dialogs.ContactBottomDialog
import com.example.whisper.vo.dialogs.Dialog
import com.example.whisper.vo.dialogs.TitleMessageDialog

fun Fragment.showDialog(dialog: Dialog) {
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
        is ContactBottomDialog -> showContactBottomDialog(dialog.bundle)
    }
}

private fun Fragment.showTitleMessageDialog(
    @StringRes title: Int = INVALID_RES,
    @StringRes message: Int = INVALID_RES,
    @StringRes positiveButtonText: Int = R.string.ok,
    @StringRes negativeButtonText: Int? = null,
    positiveButtonClickListener: DialogInterface.OnClickListener? = null,
    negativeButtonClickListener: DialogInterface.OnClickListener? = null,
    cancelable: Boolean = true
): AlertDialog = AlertDialog.Builder(context).apply {
    if (title != INVALID_RES) setTitle(title)
    if (message != INVALID_RES) setMessage(message)
    if (positiveButtonText != INVALID_RES)
        setPositiveButton(positiveButtonText, positiveButtonClickListener)
    if (negativeButtonText != null && negativeButtonText != INVALID_RES)
        setNegativeButton(negativeButtonText, negativeButtonClickListener)
    setCancelable(cancelable)
    create()
}.show()

private fun Fragment.showContactBottomDialog(bundle: Bundle) {
    ContactBottomDialogFragment()
        .apply { arguments = bundle }
        .show(
            parentFragmentManager,
            ContactBottomDialogFragment.CONTACT_BOTTOM_DIALOG_TAG
        )
}