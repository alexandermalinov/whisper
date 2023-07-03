package com.example.whisper.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.whisper.R
import com.example.whisper.utils.media.ActivityResultHandler
import java.util.Locale

/* --------------------------------------------------------------------------------------------
 * Exposed
---------------------------------------------------------------------------------------------*/
fun Fragment.navigate(destination: Destination) {
    when (destination) {
        is Internal -> {
            handleInternalNavigation(destination)
        }

        is External -> {
            handleExternalNavigation(destination)
        }
    }
}

fun Activity.navigate(destination: Destination) {
    when (destination) {
        is Internal -> {
            handleInternalNavigation(destination)
        }

        is External -> {
            handleExternalNavigation(destination)
        }
    }
}

/* --------------------------------------------------------------------------------------------
 * Private
---------------------------------------------------------------------------------------------*/
private fun Fragment.handleInternalNavigation(destination: Internal) {
    when (destination) {
        is NavGraph -> {
            if (findNavController().currentDestination?.id == id) {
                //do something here
                findNavController().popBackStack()
            }

            findNavController().navigate(
                destination.actionId,
                destination.args,
                destination.navOptions,
                destination.extras
            )
        }

        is PopBackStack -> {
            findNavController().popBackStack()
        }

        is NestedFragmentGraph -> {
            navigateToFragment(destination)
        }
    }
}

private fun Fragment.handleExternalNavigation(destination: External) {
    when (destination) {
        is GalleryNavigation -> {
            if (this is ActivityResultHandler)
                provideObserver(destination).forEach {
                    if (it.getKey() == destination.actionKey)
                        it.launch()
                }
        }

        is DocumentNavigation -> {
            if (this is ActivityResultHandler)
                provideObserver(destination).forEach {
                    if (it.getKey() == destination.actionKey)
                        it.launch()
                }
        }

        is OpenFile -> {
            Intent(Intent.ACTION_VIEW).apply {
                val extension = destination.fileName
                    .substringAfterLast('.', "")
                    .lowercase(Locale.ROOT)

                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                setDataAndType(Uri.parse(destination.filePath), mimeType)
            }.let {
                try {
                    startActivity(it)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        context?.getText(R.string.error_no_app_found_to_open_file),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        is SettingsNavigation -> {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }
    }
}

private fun Activity.handleInternalNavigation(destination: Internal) {
    when (destination) {
        is ActivityGraph -> {
            val action = Intent(this, destination.activityClass).apply {
                destination.bundle?.let { putExtras(it) }
            }
            startActivity(action)
            finish()
        }
    }
}

private fun Activity.handleExternalNavigation(destination: External) {
    // When needed, implement me
}

private fun Fragment.navigateToFragment(destination: NestedFragmentGraph) {
    /*fun getFragment(id: String) = when (id) {
        FRAGMENT_RECENT_CHATS -> RecentChatsFragment()
        FRAGMENT_CONTACS -> ContacsFragment()
        else -> throw IllegalArgumentException()
    }
    childFragmentManager.commit {
        setReorderingAllowed(true)
        replace(destination.containerViewId, getFragment(destination.fragmentId))
    }*/
}