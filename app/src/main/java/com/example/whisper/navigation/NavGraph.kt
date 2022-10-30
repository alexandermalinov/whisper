package com.example.whisper.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator

sealed class Destination

open class Internal : Destination()
open class External : Destination()

/* --------------------------------------------------------------------------------------------
 * Internal
---------------------------------------------------------------------------------------------*/
data class NavGraph(
    val actionId: Int,
    val args: Bundle? = null,
    val navOptions: NavOptions? = null,
    val extras: FragmentNavigator.Extras? = null
) : Internal()

data class ActivityGraph(
    val activityClass: Class<*>,
    val bundle: Bundle? = null
): Internal()

object PopBackStack : Internal()

class NestedFragmentGraph(
    val fragmentId: String,
    @IdRes
    val containerViewId: Int
) : Internal()

/* --------------------------------------------------------------------------------------------
 * External
---------------------------------------------------------------------------------------------*/
object GalleryNavigation : External()

object SettingsNavigation : External()
