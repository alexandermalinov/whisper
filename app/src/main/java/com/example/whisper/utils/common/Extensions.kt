package com.example.whisper.utils.common

import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

fun Fragment.grantReadUriPermission(uri: Uri) {
    context?.contentResolver?.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
}

fun Fragment.removeTransparentStatusBar() {
    requireActivity().window.clearFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
}

interface TextChangesCallback {

    fun textChanges(flow: Flow<CharSequence>)
}

interface ViewPagerChangesCallback {

    fun onPageChange(position: Int)
}

interface BottomNavigationChangesCallback {

    fun onItemChange(itemId: Int)
}

@ExperimentalCoroutinesApi
fun EditText.textChanges(): Flow<CharSequence> = callbackFlow {
    val listener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun afterTextChanged(p0: Editable?) = Unit

        override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
            trySend(p0).isSuccess
        }
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}

fun Fragment.collectState(state: suspend () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            state.invoke()
        }
    }
}