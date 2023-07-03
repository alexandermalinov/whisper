package com.example.whisper.ui.chat.peertopeer

import com.example.whisper.ui.chat.peertopeer.views.messages.IncomingTextMessage
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentPeerToPeerChatBinding
import com.example.whisper.navigation.External
import com.example.whisper.ui.base.BasePermissionFragment
import com.example.whisper.ui.chat.peertopeer.views.ChatAttachments
import com.example.whisper.ui.chat.peertopeer.views.ChatTextFieldView
import com.example.whisper.ui.chat.peertopeer.views.ChatToolbar
import com.example.whisper.ui.chat.peertopeer.views.messages.IncomingDocumentMessage
import com.example.whisper.ui.chat.peertopeer.views.messages.IncomingPhotoMessage
import com.example.whisper.ui.chat.peertopeer.views.messages.IncomingVideoMessage
import com.example.whisper.ui.chat.peertopeer.views.messages.OutgoingDocumentMessage
import com.example.whisper.ui.chat.peertopeer.views.messages.OutgoingPhotoMessage
import com.example.whisper.ui.chat.peertopeer.views.messages.OutgoingTextMessage
import com.example.whisper.ui.chat.peertopeer.views.messages.OutgoingVideoMessageView
import com.example.whisper.utils.common.grantReadUriPermission
import com.example.whisper.utils.common.hideKeyboard
import com.example.whisper.utils.media.ActivityResultHandler
import com.example.whisper.utils.media.ActivityResultObserver
import com.example.whisper.utils.media.SelectDocumentObserver
import com.example.whisper.utils.media.SelectImageObserver
import com.example.whisper.utils.permissions.PermissionStateHandler
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiModel
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType.Text.isFileType
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PeerToPeerChatFragment : BasePermissionFragment<FragmentPeerToPeerChatBinding>(),
    ActivityResultHandler {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: PeerToPeerChatViewModel by viewModels()
    private lateinit var selectImageObserver: SelectImageObserver
    private lateinit var selectDocumentObserver: SelectDocumentObserver

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        setActivityResultObservers()
        setObservers()
        collectNavigation(viewModel.navigationFlow)
        collectPermission(viewModel.permissionState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = DataBindingUtil.inflate(
            inflater,
            getLayoutId(),
            container,
            false
        )

        return dataBinding.root
    }

    override fun providePermissionStateHandler(): PermissionStateHandler = viewModel

    override fun getLayoutId(): Int = R.layout.fragment_peer_to_peer_chat

    override fun provideObserver(destination: External): List<ActivityResultObserver> = listOf(
        selectImageObserver,
        selectDocumentObserver
    )

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/

    @Composable
    private fun ChatScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.black))
        ) {
            val state by viewModel.uiState.collectAsState()
            val storageImages by viewModel.storageImages.collectAsState()
            var shouldOpenAttachments by remember { mutableStateOf(false) }

            ChatToolbar(state) { viewModel.onBackClicked() }
            Messages()
            ChatTextFieldView(
                state = state,
                sendMessage = { viewModel.sendMessage() },
                openAttachments = {
                    shouldOpenAttachments = shouldOpenAttachments.not()
                    if (shouldOpenAttachments) viewModel.requestPermission()
                    this@PeerToPeerChatFragment.view?.hideKeyboard()
                },
                updateMessageBody = { messageBody -> viewModel.updateMessageBody(messageBody) },
                onTextFieldClick = {
                    shouldOpenAttachments = false
                }
            )

            AnimatedVisibility(
                visible = shouldOpenAttachments,
                enter = expandVertically(animationSpec = spring()),
                exit = shrinkVertically(animationSpec = spring())
            ) {
                ChatAttachments(
                    images = storageImages.images,
                    getSelectionIndex = { viewModel.getSelectedImageIndex(it) },
                    isImageSelected = { viewModel.getSelectedImageIndex(it) != null },
                    toggleImageSelection = { viewModel.toggleImageSelection(it) },
                    onChooseGalleryImage = { viewModel.onChooseGalleryImageClicked() },
                    onChooseDocument = { viewModel.onChooseDocument() }
                )
            }
        }
    }

    @Composable
    private fun <T> T.AnimationBox(
        isNewMessage: Boolean,
        content: @Composable (T.() -> Unit)
    ) {
        val state = remember {
            MutableTransitionState(false).apply {
                targetState = true
            }
        }

        AnimatedVisibility(
            visibleState = state,
            enter = slideInHorizontally(animationSpec = keyframes {
                this.durationMillis = if (isNewMessage) 150 else 0
            }) + slideInVertically(animationSpec = keyframes {
                this.durationMillis = if (isNewMessage) 250 else 0
            }) + fadeIn(animationSpec = keyframes {
                this.durationMillis = if (isNewMessage) 250 else 0
            }),
            exit = fadeOut()
        ) { content() }
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun ColumnScope.Messages() {
        val listState = rememberLazyListState()
        val messagesState by viewModel.messages.collectAsState()
        var previousMessageCount by remember { mutableStateOf(messagesState.size) }

        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp, top = 24.dp)
        ) {
            items(
                items = messagesState,
                key = { message -> message.createdAt },
                itemContent = { message ->
                    AnimationBox(previousMessageCount != messagesState.size) {
                        when {
                            message is MessageUiModel.OutgoingMessageUiModel -> {
                                OutgoingTextMessage(
                                    body = message.text,
                                    timestamp = message.createdAt,
                                    status = message.status,
                                    showStatus = messagesState.indexOf(message) == 0
                                )
                            }

                            message is MessageUiModel.IncomingMessageUiModel -> {
                                IncomingTextMessage(
                                    body = message.text,
                                    timestamp = message.createdAt
                                )
                            }

                            message is MessageUiModel.OutgoingFileMessageUiModel &&
                                    message.type is MessageUiType.Photo -> {
                                OutgoingPhotoMessage(
                                    fileUrl = message.fileUrl,
                                    status = message.status,
                                    showStatus = messagesState.indexOf(message) == 0
                                )
                            }

                            message is MessageUiModel.IncomingFileMessageUiModel &&
                                    message.type is MessageUiType.Photo -> {
                                IncomingPhotoMessage(fileUrl = message.fileUrl)
                            }

                            message is MessageUiModel.OutgoingVideoMessageUiModel -> {
                                OutgoingVideoMessageView(
                                    messageState = message,
                                    showStatus = messagesState.indexOf(message) == 0,
                                    playVideo = { viewModel.onPlayVideoClicked(message.id) },
                                    pauseVideo = { viewModel.onPauseVideoClicked(message.id) }
                                )
                            }

                            message is MessageUiModel.IncomingVideoMessageUiModel -> {
                                IncomingVideoMessage(fileUrl = message.fileUrl)
                            }

                            message is MessageUiModel.OutgoingFileMessageUiModel &&
                                    message.type.isFileType() -> {
                                OutgoingDocumentMessage(
                                    type = message.type,
                                    timestamp = message.createdAt,
                                    status = message.status,
                                    showStatus = messagesState.indexOf(message) == 0,
                                    onMessageClick = {
                                        message.type.name?.let {
                                            viewModel.openFile(message.fileUrl, it)
                                        }
                                    }
                                )
                            }

                            message is MessageUiModel.IncomingFileMessageUiModel &&
                                    message.type.isFileType() -> {
                                IncomingDocumentMessage(
                                    type = message.type,
                                    timestamp = message.createdAt,
                                    onMessageClick = {
                                        message.type.name?.let {
                                            viewModel.openFile(message.fileUrl, it)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }

        LaunchedEffect(messagesState.size) {
            listState.animateScrollToItem(0)
            previousMessageCount = messagesState.size
            viewModel.markMessagesAsRead()
        }
    }

    @Preview
    @Composable
    fun DefaultPreview() {
        ChatScreen()
    }

    private fun initUi() {
        dataBinding.composeViewChat.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    ChatScreen()
                }
            }
        }
    }

    private fun setActivityResultObservers() {
        selectImageObserver = SelectImageObserver(requireActivity().activityResultRegistry) { uri ->
            uri?.let {
                grantReadUriPermission(it)
                val fileInformation = viewModel.getFileNameAndSize(it)
                val fileName = fileInformation.first
                val fileSize = fileInformation.second
                viewModel.sendMessage(MessageUiType.Photo(it, fileName, fileSize))
            }
        }

        selectDocumentObserver =
            SelectDocumentObserver(requireActivity().activityResultRegistry) { uri ->
                uri?.let {
                    grantReadUriPermission(it)
                    viewModel.handleFileSending(uri)
                }
            }
    }

    private fun setObservers() {
        with(viewLifecycleOwner.lifecycle) {
            addObserver(selectImageObserver)
            addObserver(selectDocumentObserver)
        }
    }
}