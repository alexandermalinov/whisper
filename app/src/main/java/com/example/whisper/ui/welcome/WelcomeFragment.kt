package com.example.whisper.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentWelcomeBinding
import com.example.whisper.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: WelcomeViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        collectNavigation(viewModel.navigationFlow)
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUi() {
        dataBinding.presenter = viewModel
    }

    override fun getLayoutId(): Int = R.layout.fragment_welcome
}