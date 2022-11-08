package com.example.whisper.ui.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentSignUpStepOneBinding
import com.example.whisper.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpStepOneFragment : BaseFragment<FragmentSignUpStepOneBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: SignUpViewModel by activityViewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        observeLiveData()
        observeNavigation(viewModel.navigationLiveData)
    }

    /* --------------------------------------------------------------------------------------------
     * Private
   ---------------------------------------------------------------------------------------------*/
    private fun initUi() {
        dataBinding.presenter = viewModel
    }

    private fun observeLiveData() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiModel ->
            dataBinding.model = uiModel
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_sign_up_step_one
}