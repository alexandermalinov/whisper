package com.example.whisper.ui.basecontacts

import com.example.whisper.utils.common.ViewPagerChangesCallback


interface BaseContactsPresenter : ViewPagerChangesCallback {

    fun addContactOrCreateGroup()
}