package com.example.whisper.ui.signin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentSignInBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectLatestFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: SignInViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        collectNavigation(viewModel.navigationFlow)
        collectDialogFlow(viewModel.dialogFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_sign_in

    /* --------------------------------------------------------------------------------------------
     * Private
   ---------------------------------------------------------------------------------------------*/
    private fun initUi() {
        dataBinding.presenter = viewModel
        observeLiveData()
    }

    private fun observeLiveData() {
        collectLatestFlow(viewModel.uiState) { uiModel ->
            dataBinding.model = uiModel
        }
    }
}