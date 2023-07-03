package com.example.whisper.ui.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentSignUpStepTwoBinding
import com.example.whisper.navigation.External
import com.example.whisper.ui.base.BasePermissionFragment
import com.example.whisper.utils.common.collectLatestFlow
import com.example.whisper.utils.common.grantReadUriPermission
import com.example.whisper.utils.media.ActivityResultHandler
import com.example.whisper.utils.media.ActivityResultObserver
import com.example.whisper.utils.media.SelectImageObserver
import com.example.whisper.utils.permissions.PermissionStateHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpStepTwoFragment : BasePermissionFragment<FragmentSignUpStepTwoBinding>(),
    ActivityResultHandler {

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
        dataBinding.presenter = viewModel
        setImagePicker()
        setObservers()
        collectState()
    }

    override fun providePermissionStateHandler(): PermissionStateHandler? = viewModel

    override fun getLayoutId(): Int = R.layout.fragment_sign_up_step_two

    override fun provideObserver(destination: External): List<ActivityResultObserver> =
        listOf(selectImageObserver)

    /* --------------------------------------------------------------------------------------------
     * Private
   ---------------------------------------------------------------------------------------------*/
    private fun collectState() {
        collectLatestFlow(viewModel.uiState) { uiModel ->
            dataBinding.model = uiModel
        }

        collectNavigation(viewModel.navigationFlow)
        collectDialogFlow(viewModel.dialogFlow)
        collectPermission(viewModel.permissionState)
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