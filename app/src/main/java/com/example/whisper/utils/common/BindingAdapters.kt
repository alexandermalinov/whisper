package com.example.whisper.utils.common

import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.whisper.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi

@BindingAdapter("visibleGone")
fun View.setVisibility(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

@BindingAdapter("inputError")
fun TextInputLayout.setError(@StringRes error: Int) {
    if (error != INVALID_RES)
        setError(resources.getString(error))
    else
        setError(null)
}

@BindingAdapter("glideRes", "defaultGlideRes")
fun View.setGlideRes(
    glideRes: String?,
    @DrawableRes defaultGlideRes: Int
) {
    Glide.with(this)
        .load(glideRes)
        .error(defaultGlideRes)
        .placeholder(defaultGlideRes)
        .let { drawable -> if (!glideRes.isNullOrBlank()) drawable.centerCrop() else drawable }
        .into(this as ImageView)
}

@BindingAdapter("uriRes")
fun ShapeableImageView.setUriRes(uri: Uri?) {
    if (uri != null && uri.toString() != EMPTY)
        setImageURI(uri)
    else
        setImageResource(R.drawable.ic_user)
}

@BindingAdapter("safeText")
fun TextView.setSafeText(value: Int) {
    text = value.toString()
}

@BindingAdapter("textFormatted")
fun TextView.setTextFormatted(textRes: TextRes) {
    with(textRes) {
        setText(
            if (textResource != INVALID_RES && text.isNullOrBlank().not()) {
                resources.getString(textResource, text)
            } else {
                EMPTY
            }
        )
    }
}

@BindingAdapter("boldText")
fun TextView.setTextBold(shouldBoldText: Boolean) {
    if (shouldBoldText) {
        setTypeface(null, Typeface.BOLD)
        setTextColor(resources.getColor(R.color.black))
    }
}

@ExperimentalCoroutinesApi
@BindingAdapter("textChanges")
fun EditText.setTextChange(callback: TextChangesCallback) {
    callback.textChanges(textChanges())
}