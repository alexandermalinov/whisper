package com.example.whisper.ui.addcontact

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.R
import com.example.whisper.databinding.FragmentAddContactBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddContactFragment : BaseFragment<FragmentAddContactBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: AddContactViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiData()
        observeNavigation(viewModel.navigationLiveData)
        observeDialogLiveData(viewModel.dialogLiveData)
    }

    override fun getLayoutId(): Int = R.layout.fragment_add_contact

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        initConnectionsRecyclerView()
        collectUiStates()
    }

    private fun initConnectionsRecyclerView() {
        dataBinding.recyclerContacts.apply {
            adapter = AddContactAdapter(viewModel)
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun collectUiStates() {
        collectState {
            viewModel.uiState.collect { uiState ->
                (dataBinding.recyclerContacts.adapter as AddContactAdapter).submitList(uiState.contacts)
            }
        }
    }
}