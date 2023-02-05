package com.example.whisper.ui.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentSignUpStepOneBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectState
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
        observeDialogLiveData(viewModel.dialogLiveData)
    }

    override fun getLayoutId(): Int = R.layout.fragment_sign_up_step_one

    /* --------------------------------------------------------------------------------------------
     * Private
   ---------------------------------------------------------------------------------------------*/
    private fun initUi() {
        dataBinding.presenter = viewModel
    }

    private fun observeLiveData() {
        collectState {
            viewModel.uiState.collect { uiModel ->
                dataBinding.model = uiModel
            }
        }
    }
}