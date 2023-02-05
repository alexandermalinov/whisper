package com.example.whisper.ui.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.whisper.R
import com.example.whisper.databinding.FragmentSignUpStepTwoBinding
import com.example.whisper.navigation.External
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectState
import com.example.whisper.utils.common.grantReadUriPermission
import com.example.whisper.utils.media.ActivityResultHandler
import com.example.whisper.utils.media.SelectImageObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStepTwoFragment : BaseFragment<FragmentSignUpStepTwoBinding>(), ActivityResultHandler {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: SignUpViewModel by activityViewModels()
    private lateinit var selectImageObserver: SelectImageObserver

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setImagePicker()
        setObservers()
        observeLiveData()
        observeNavigation(viewModel.navigationLiveData)
        observeDialogLiveData(viewModel.dialogLiveData)
    }

    override fun getLayoutId(): Int = R.layout.fragment_sign_up_step_two

    override fun provideObserver(destination: External) = selectImageObserver

    /* --------------------------------------------------------------------------------------------
     * Private
   ---------------------------------------------------------------------------------------------*/
    private fun observeLiveData() {
        dataBinding.presenter = viewModel
        collectState {
            viewModel.uiState.collect { uiModel ->
                dataBinding.model = uiModel
            }
        }
    }

    private fun setObservers() {
        with(viewLifecycleOwner.lifecycle) {
            addObserver(selectImageObserver)
        }
    }

    private fun setImagePicker() {
        selectImageObserver = SelectImageObserver(requireActivity().activityResultRegistry) {
            it?.let { uri ->
                viewModel.setProfilePicture(uri)
                grantReadUriPermission(uri)
            }
        }
    }
}