package com.example.whisper.ui.main

import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val userRepository: UserRepository) :
    BaseViewModel() {

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun setBottomNavigationVisibility(
        navController: NavController,
        view: View
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.recentChatsFragment,
                R.id.profileFragment,
                R.id.contactsFragment -> view.makeVisible()
                else -> view.visibility = View.GONE
            }
        }
    }

    fun getCurrentUserProfilePicture(getPicture: (String) -> Unit) {
        viewModelScope.launch {
            val id = userRepository.getLoggedUserId()
            getPicture.invoke(userRepository.getLoggedUser(id).userPicture)
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun View.makeVisible() {
        visibility = View.VISIBLE
    }
}