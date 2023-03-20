package com.example.whisper.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentProfileBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.ui.contacts.ContactsAdapter
import com.example.whisper.ui.contacts.ContactsInviteAdapter
import com.example.whisper.ui.contacts.ContactsPendingAdapter
import com.example.whisper.utils.common.collectState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: ProfileViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectUiStates()
        observeNavigation(viewModel.navigationLiveData)
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        //dataBinding.presenter = viewModel
        //lifecycle.addObserver(viewModel)

    }
    private fun collectUiStates() {
        collectState {
            viewModel.uiState.collect { uiState ->
                dataBinding.model = uiState
            }
        }
    }
}