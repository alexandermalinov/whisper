package com.example.whisper.ui.recentchats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.R
import com.example.whisper.databinding.FragmentRecentChatsBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectLatestFlow
import com.example.whisper.vo.recentchats.ChatsRecyclerViewState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RecentChatsFragment : BaseFragment<FragmentRecentChatsBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: RecentChatsViewModel by viewModels()
    private lateinit var linearLayoutManager: LinearLayoutManager

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiData()
        initConnectionsRecyclerView()
        collectUiStates()
        collectNavigation(viewModel.navigationFlow)
        collectDialogFlow(viewModel.dialogFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_recent_chats

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        dataBinding.presenter = viewModel
        lifecycle.addObserver(viewModel)
        initConnectionsRecyclerView()
    }

    private fun initConnectionsRecyclerView() {
        linearLayoutManager = LinearLayoutManager(context)
        dataBinding.recyclerRecentChats.apply {
            adapter = RecentChatsAdapter(viewModel)
            layoutManager = linearLayoutManager
        }

        dataBinding.recyclerPinnedChats.apply {
            adapter = PinnedChatsAdapter(viewModel)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        initScrollListener()
    }

    private fun collectUiStates() {
        collectLatestFlow(viewModel.uiState) { uiState ->
            dataBinding.model = uiState
        }

        collectLatestFlow(viewModel.pinnedChats) { pinnedChats ->
            (dataBinding.recyclerPinnedChats.adapter as PinnedChatsAdapter)
                .submitList(pinnedChats)
        }

        collectLatestFlow(viewModel.recentChats) { recentChats ->
            (dataBinding.recyclerRecentChats.adapter as RecentChatsAdapter)
                .submitList(recentChats)
        }

        collectLatestFlow(viewModel.chatsRecyclerViewState) { state ->
            when (state) {
                ChatsRecyclerViewState.SCROLL_TO_TOP -> {
                    dataBinding.recyclerRecentChats.scrollToPosition(0)
                }
                else -> {
                    // do nothing
                }
            }
        }
    }

    private fun initScrollListener() {
        dataBinding.apply {
            recyclerRecentChats.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    viewModel.setIsAtTheTop(linearLayoutManager.findFirstVisibleItemPosition() == 0)
                }
            })
        }
    }
}