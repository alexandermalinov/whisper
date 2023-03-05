package com.example.whisper.utils.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.whisper.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
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
    if (value != INVALID_RES) text = value.toString()
}

@BindingAdapter("textFormatted")
fun TextView.setTextFormatted(textRes: TextRes) {
    with(textRes) {
        setText(
            when {
                textResource != INVALID_RES && text.isNullOrBlank().not() -> {
                    resources.getString(textResource, text)
                }
                text.isNullOrBlank().not() -> {
                    text
                }
                else -> {
                    EMPTY
                }
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
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

@BindingAdapter("showKeyboard")
fun EditText.setShowKeyboard(showKeyboard: Boolean) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

@BindingAdapter("lastMessage")
fun TextView.setLastMessage(message: String) {
    text = when {
        message.isBlank() -> resources.getString(R.string.no_messages)
        else -> message
    }
}

@BindingAdapter("unreadMessagesCount")
fun TextView.setLastMessage(unreadMessagesCount: Int) {
    text = when {
        unreadMessagesCount < 10 -> unreadMessagesCount.toString()
        else -> resources.getString(R.string.nine_and_more_messages)
    }
}

@BindingAdapter("navigationIconId", "menuItemId", "DefaultIconId")
fun BottomNavigationView.setNavigationIcon(
    navigationIconUrl: String?,
    menuItemId: Int?,
    defaultIconId: Int
) {
    if (navigationIconUrl == null || menuItemId == null) return
    val menuItem = menu.findItem(menuItemId)

    Glide.with(this)
        .asBitmap()
        .load(navigationIconUrl)
        .apply(
            RequestOptions.circleCropTransform()
                .placeholder(defaultIconId)
        )
        .into(object : CustomTarget<Bitmap>() {

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                if (resource.byteCount != 0) {
                    menuItem?.icon = BitmapDrawable(resources, resource)
                } else {
                    menuItem?.icon = ContextCompat.getDrawable(context, defaultIconId)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}

@BindingAdapter("viewPagerOnPageChange")
fun ViewPager2.setViewPagerCurrentItem(callback: ViewPagerChangesCallback?) {
    if (callback == null) return
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            callback.onPageChange(position)
        }
    })
}

@BindingAdapter("selectViewPagerPage")
fun ViewPager2.selectViewPagerPage(position: Int) {
    currentItem = position
}

@BindingAdapter("bottomNavigationOnItemChange")
fun BottomNavigationView.setOnBottomNavigationItemChange(callback: BottomNavigationChangesCallback) {
    setOnItemSelectedListener {
            val position = when (it.itemId) {
                R.id.recentChatsFragment -> RECENT_CHATS_PAGER_POSITION
                R.id.contactsFragment -> CONTACTS_CHATS_PAGER_POSITION
                else -> PROFILE_CHATS_PAGER_POSITION
            }
            callback.onItemChange(position)
        return@setOnItemSelectedListener true
    }
}

@BindingAdapter("checkBottomNavigationItem")
fun BottomNavigationView.checkBottomNavigationItem(itemId: Int) {
    menu.findItem(itemId).isChecked = true
}

@BindingAdapter("buttonIconId")
fun CheckBox.setButtonIconId(icon: Int) {
    setButtonDrawable(icon)
}