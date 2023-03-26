package com.example.whisper.ui.utils.dialogs.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentContactBottomDialogBinding
import com.example.whisper.utils.common.collectState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactBottomDialogFragment : BottomSheetDialogFragment() {

    /* --------------------------------------------------------------------------------------------
     * Properties
   ---------------------------------------------------------------------------------------------*/
    private lateinit var dataBinding: FragmentContactBottomDialogBinding
    private val viewModel: ContactBottomDialogViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
   ---------------------------------------------------------------------------------------------*/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_contact_bottom_dialog,
            container,
            false
        )
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataBinding.presenter = viewModel
        collectUiState()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    /* --------------------------------------------------------------------------------------------
     * Private
   ---------------------------------------------------------------------------------------------*/
    private fun collectUiState() {
        collectState {
            viewModel.uiState.collect { uiModel ->
                dataBinding.model = uiModel
            }
        }

        collectState {
            viewModel.dismissDialog.collect { shouldDismissDialog ->
                if (shouldDismissDialog) dismissNow()
            }
        }
    }

    companion object {
        const val CONTACT_BOTTOM_DIALOG_TAG = "CONTACT_BOTTOM_DIALOG_TAG"
    }
}