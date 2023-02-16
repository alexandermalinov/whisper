package com.example.whisper.utils.common

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.whisper.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


@BindingAdapter("visibleGone")
fun View.setVisibility(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

@BindingAdapter("visibleInvisible")
fun View.setVisibleInvisible(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.INVISIBLE
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
    defaultGlideRes: Int
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
        setImageResource(R.drawable.sp_profile_picture)
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

@BindingAdapter("audioTime")
fun TextView.setAudioTime(time: Long) {
    val hms = String.format(
        "%02d:%02d:%02d.%03d",
        TimeUnit.MILLISECONDS.toHours(time),
        TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1),
        time % 1000
    )
    text = hms
}

@BindingAdapter("audioProgress", "audioDuration")
fun SeekBar.setAudioTime(time: Long, audioDuration: Long) {
    progress = (time - audioDuration).absoluteValue.toInt()
}

@BindingAdapter("audioMaxValue")
fun SeekBar.setAudioMaxValue(maxValue: Int) {
    max = maxValue
}

@BindingAdapter("startShimmerAnimation")
fun ShimmerFrameLayout.setStartShimmerAnimation(startShimmer: Boolean) {
    if (startShimmer) startShimmer() else stopShimmer()
}

@BindingAdapter("hideKeyboard")
fun EditText.setHideKeyboard(hideKeyboard: Boolean) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

@BindingAdapter("showKeyboard")
fun EditText.setShowKeyboard(showKeyboard: Boolean) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}