package com.example.whisper.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentProfileBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectLatestFlow
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
        initUiData()
        collectUiStates()
        collectNavigation(viewModel.navigationFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_profile

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        dataBinding.presenter = viewModel
    }

    private fun collectUiStates() {
        collectLatestFlow(viewModel.uiState) { uiState ->
            dataBinding.model = uiState
        }
    }
}