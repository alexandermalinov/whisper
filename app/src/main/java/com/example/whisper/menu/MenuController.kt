package com.connection.menu

import android.content.Context
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import com.example.whisper.R

fun Context.showMenu(menu: MenuUiModel, menuIcon: View) {
    when (menu) {
        is PopupMenuUiModel -> showPopupMenu(menu.layout, menuIcon) {
            menu.onClickListener?.invoke(it.itemId)
            true
        }
    }
}

fun Context.showPopupMenu(
    @MenuRes
    menu: Int,
    menuIcon: View,
    onClickListener: PopupMenu.OnMenuItemClickListener
) = PopupMenu(ContextThemeWrapper(this, R.style.PopupMenu), menuIcon).apply {
    inflate(menu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)
    setOnMenuItemClickListener(onClickListener)
    show()
}