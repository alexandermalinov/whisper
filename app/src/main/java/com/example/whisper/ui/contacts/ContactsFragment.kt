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
        dataBinding.recyclerInvitations2.apply {
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
                val invi = if (contacts.isNotEmpty()) listOf(contacts[0]) else listOf()
                (dataBinding.recyclerInvitations.adapter as ContactsInviteAdapter).submitList(
                    invi
                )
                (dataBinding.recyclerInvitations2.adapter as ContactsInviteAdapter).submitList(
                    contacts
                )
            }
        }

        collectState {
            viewModel.invitationsExpandEvent.collect { shouldExpand ->
                expandInvitations(shouldExpand)
            }
        }
    }

    private fun expandInvitations(shouldExpand: Boolean) {
        dataBinding.apply {
            TransitionManager.beginDelayedTransition(recyclerInvitations, AutoTransition())
            recyclerInvitations2.visibility = if (shouldExpand) View.VISIBLE else View.GONE
            buttonExpand.visibility = if (shouldExpand) View.GONE else View.VISIBLE
            buttonShrink.visibility = if (shouldExpand) View.VISIBLE else View.GONE
            //val params = recyclerInvitations.layoutParams
            //params.width = ViewGroup.LayoutParams.MATCH_PARENT
            //params.height = if (shouldExpand) ViewGroup.LayoutParams.WRAP_CONTENT else 240
            //recyclerInvitations.layoutParams = params
        }
    }
}