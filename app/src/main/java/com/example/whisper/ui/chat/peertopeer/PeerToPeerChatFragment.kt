package com.example.whisper.ui.chat.peertopeer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentPeerToPeerChatBinding
import com.example.whisper.ui.base.BasePermissionFragment
import com.example.whisper.utils.common.collectLatestFlow
import com.example.whisper.utils.permissions.PermissionStateHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PeerToPeerChatFragment : BasePermissionFragment<FragmentPeerToPeerChatBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: PeerToPeerChatViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiData()
        collectUiStates()
        initVoiceButtonListener()
        collectNavigation(viewModel.navigationFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_peer_to_peer_chat

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initUiData() {
        dataBinding.presenter = viewModel
    }

    private fun collectUiStates() {
        collectLatestFlow(viewModel.uiState) { uiState ->
            dataBinding.model = uiState
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initVoiceButtonListener() {
        dataBinding.buttonVoice.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> viewModel.onActionDown()
                MotionEvent.ACTION_UP -> viewModel.stopRecording()
            }

            return@setOnTouchListener false
        }
    }

    override fun providePermissionStateHandler(): PermissionStateHandler = viewModel
}