package com.example.whisper.ui.contacts

import android.animation.LayoutTransition
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whisper.R
import com.example.whisper.databinding.FragmentContactsBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsFragment : BaseFragment<FragmentContactsBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: ContactsViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiData()
        collectUiStates()
        observeNavigation(viewModel.navigationLiveData)
    }

    override fun getLayoutId(): Int = R.layout.fragment_contacts

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        dataBinding.presenter = viewModel
        initConnectionsRecyclerView()
    }

    private fun initConnectionsRecyclerView() {
        dataBinding.recyclerContacts.apply {
            adapter = ContactsAdapter(viewModel)
            layoutManager = LinearLayoutManager(context)
        }

        dataBinding.recyclerInvitations.apply {
            adapter = ContactsInviteAdapter(viewModel)
            layoutManager = LinearLayoutManager(context)
        }

        dataBinding.recyclerPending.apply {
            adapter = ContactsInviteAdapter(viewModel)
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun collectUiStates() {
        collectState {
            viewModel.uiState.collect { uiState ->
                dataBinding.model = uiState
            }
        }

        collectState {
            viewModel.contacts.collect { contacts ->
                (dataBinding.recyclerContacts.adapter as ContactsAdapter).submitList(contacts)
                (dataBinding.recyclerInvitations.adapter as ContactsInviteAdapter).submitList(contacts)
                (dataBinding.recyclerPending.adapter as ContactsInviteAdapter).submitList(contacts)
            }
        }

        collectState {
            viewModel.invitationsExpandEvent.collect { shouldExpand ->
                expandInvitations(shouldExpand)
            }
        }

        collectState {
            viewModel.pendingExpandEvent.collect { shouldExpand ->
                expandPending(shouldExpand)
            }
        }
    }

    private fun expandInvitations(shouldExpand: Boolean) {
        dataBinding.apply {
            TransitionManager.beginDelayedTransition(recyclerInvitations, AutoTransition())
            recyclerInvitations.visibility = if (shouldExpand) View.VISIBLE else View.GONE
            buttonExpand.visibility = if (shouldExpand) View.GONE else View.VISIBLE
            buttonShrink.visibility = if (shouldExpand) View.VISIBLE else View.GONE
        }
    }

    private fun expandPending(shouldExpand: Boolean) {
        dataBinding.apply {
            TransitionManager.beginDelayedTransition(recyclerInvitations, AutoTransition())
            recyclerPending.visibility = if (shouldExpand) View.VISIBLE else View.GONE
            buttonPendingExpand.visibility = if (shouldExpand) View.GONE else View.VISIBLE
            buttonPendingCollapse.visibility = if (shouldExpand) View.VISIBLE else View.GONE
        }
    }
}