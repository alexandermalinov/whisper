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
    private val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(context)

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiData()
        observeNavigation(viewModel.navigationFlow)
        observeDialogFlow(viewModel.dialogFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_add_contact

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        dataBinding.presenter = viewModel
        initConnectionsRecyclerView()
        collectUiStates()
    }

    private fun initConnectionsRecyclerView() {
        dataBinding.recyclerContacts.apply {
            adapter = AddContactAdapter(viewModel)
            layoutManager = linearLayoutManager
        }
    }

    private fun collectUiStates() {
        collectState {
            viewModel.uiState.collect { uiState ->
                dataBinding.model = uiState
            }
        }

        collectState {
            viewModel.users.collect { users ->
                (dataBinding.recyclerContacts.adapter as AddContactAdapter).submitList(users)
            }
        }

        collectState {
            viewModel.addContactEvents.collect { event ->
                dataBinding.event = event
            }
        }
    }
}