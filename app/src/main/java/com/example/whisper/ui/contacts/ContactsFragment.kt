package com.example.whisper.ui.contacts

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
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
        observeNavigation(viewModel.navigationFlow)
        observeDialogFlow(viewModel.dialogFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_contacts

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        dataBinding.presenter = viewModel
        lifecycle.addObserver(viewModel)
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
            adapter = ContactsPendingAdapter(viewModel)
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
                (dataBinding.recyclerContacts.adapter as ContactsAdapter)
                    .submitList(contacts)
            }
        }

        collectState {
            viewModel.invitations.collect { invitations ->
                (dataBinding.recyclerInvitations.adapter as ContactsInviteAdapter)
                    .submitList(invitations)
            }
        }

        collectState {
            viewModel.pending.collect { pendingContacts ->
                (dataBinding.recyclerPending.adapter as ContactsPendingAdapter)
                    .submitList(pendingContacts)
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
        if (viewModel.invitations.value.isEmpty()) return

        dataBinding.apply {
            linearLayoutInvitations.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            TransitionManager.beginDelayedTransition(recyclerInvitations, AutoTransition())
            recyclerInvitations.visibility = if (shouldExpand) View.VISIBLE else View.GONE

            ObjectAnimator.ofFloat(buttonExpand, "rotation", 0f, 180f)
                .setDuration(300)
                .start()

            if (shouldExpand) {
                buttonExpand.setButtonDrawable(R.drawable.ic_arrow_down)
            } else {
                buttonExpand.setButtonDrawable(R.drawable.ic_arrow_up)
            }
        }
    }

    private fun expandPending(shouldExpand: Boolean) {
        if (viewModel.pending.value.isEmpty()) return

        dataBinding.apply {
            linearLayoutPending.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            TransitionManager.beginDelayedTransition(recyclerPending, AutoTransition())
            recyclerPending.visibility = if (shouldExpand) View.VISIBLE else View.GONE

            ObjectAnimator.ofFloat(buttonPendingExpand, "rotation", 0f, 180f)
                .setDuration(300)
                .start()

            if (shouldExpand) {
                buttonPendingExpand.setButtonDrawable(R.drawable.ic_arrow_down)
            } else {
                buttonPendingExpand.setButtonDrawable(R.drawable.ic_arrow_up)
            }
        }
    }
}