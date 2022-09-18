package com.example.whisper.navigation

import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.connection.utils.image.ActivityResultHandler

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

/* --------------------------------------------------------------------------------------------
 * Private
---------------------------------------------------------------------------------------------*/
private fun Fragment.handleInternalNavigation(destination: Internal) {
    when (destination) {
        is NavigationGraph -> {
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
                provideObserver(destination).launch()
        }
        is SettingsNavigation -> {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }
    }
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