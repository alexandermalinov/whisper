package com.example.whisper.ui.chat.peertopeer

import android.Manifest
import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.whisper.data.VideoPlayer
import com.example.whisper.data.AudioRecorder
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.images.ImagesRepository
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.messages.GetChatMessagesUseCase
import com.example.whisper.domain.messages.SendFileMessageUseCase
import com.example.whisper.domain.messages.SendTextMessageUseCase
import com.example.whisper.navigation.DocumentNavigation
import com.example.whisper.navigation.GalleryNavigation
import com.example.whisper.navigation.OpenFile
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.ui.contacts.OnlineStatus
import com.example.whisper.utils.FileUtils
import com.example.whisper.utils.NetworkHandler
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.SELECT_DOCUMENT_KEY
import com.example.whisper.utils.common.SELECT_IMAGE_KEY
import com.example.whisper.utils.common.ZERO
import com.example.whisper.utils.permissions.DeniedState
import com.example.whisper.utils.permissions.GrantedState
import com.example.whisper.utils.permissions.Permission
import com.example.whisper.utils.permissions.PermissionChecker
import com.example.whisper.utils.permissions.PermissionRequest
import com.example.whisper.utils.permissions.PermissionState
import com.example.whisper.utils.permissions.PermissionStateHandler
import com.example.whisper.vo.chat.peertopeer.AttachmentsUiModel
import com.example.whisper.vo.chat.peertopeer.PeerToPeerChatUiModel
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiModel
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType
import com.example.whisper.vo.chat.peertopeer.messages.VideoMessageStates
import com.example.whisper.vo.recentchats.RecentChatUiModel
import com.sendbird.android.GroupChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PeerToPeerChatViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val imagesRepository: ImagesRepository,
    private val messageRepository: MessagesRepository,
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository,
    private val userRepository: UserRepository,
    private val networkHandler: NetworkHandler,
    private val permissionChecker: PermissionChecker,
    private val fileUtils: FileUtils
) : BaseViewModel(), PeerToPeerChatPresenter, MessagesPresenter, PermissionStateHandler {

    val uiState
        get() = _uiState.asStateFlow()

    val messages
        get() = _messages.asStateFlow()

    val storageImages
        get() = _storageImages.asStateFlow()

    val permissionState
        get() = _permissionState.asSharedFlow()

    private val _uiState = MutableStateFlow(PeerToPeerChatUiModel())
    private val _messages = MutableStateFlow(listOf<MessageUiModel>())
    private val _storageImages = MutableStateFlow(AttachmentsUiModel())
    private val _permissionState = MutableSharedFlow<Permission>()

    private val audioRecorder = AudioRecorder(application)
    private val audioPlayer = VideoPlayer()
    private val videoPlayer = VideoPlayer()
    private var audioTimeRecorder: CountDownTimer? = null
    private var audioPlayRecorder: CountDownTimer? = null

    private var groupChannel: GroupChannel? = null
    private var loggedUserId: String? = null

    init {
        initUiState()
        collectMessages()
        initGroupChannel()
    }

    override fun onBackClicked() {
        viewModelScope.launch {
            _navigationFlow.emit(PopBackStack)
        }
    }

    override fun sendMessage(messageType: MessageUiType?) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = _uiState.value.messageBody
            val selectedImages = _storageImages.value.selectedImages

            withContext(Dispatchers.Main) {
                _uiState.emit(_uiState.value.copy(messageBody = EMPTY))
                _storageImages.emit(_storageImages.value.copy(selectedImages = emptyList()))
            }

            withContext(Dispatchers.IO) {
                when {
                    messageType is MessageUiType.Text -> {
                        sendTextMessage(message)
                    }

                    messageType == null && message.isNotEmpty() -> {
                        sendTextMessage(message)
                    }

                    messageType == null -> {
                        sendFileMessage(messageType, selectedImages)
                    }

                    else -> {
                        sendFileMessage(messageType, selectedImages)
                    }
                }
            }
        }
    }

    override fun onChooseGalleryImageClicked() {
        viewModelScope.launch {
            _navigationFlow.emit(GalleryNavigation(SELECT_IMAGE_KEY))
        }
    }

    override fun onChooseDocument() {
        viewModelScope.launch {
            _navigationFlow.emit(DocumentNavigation(SELECT_DOCUMENT_KEY))
        }
    }

    override fun onRecord(): Boolean {
        viewModelScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    isVoiceButtonVisible = false,
                    recordedTime = MAX_AUDIO_RECORD_TIME,
                    isRecordTimeVisible = true,
                    isRecordTimeSeekbarVisible = true,
                    recordTimeSeekbarMaxValue = MAX_AUDIO_RECORD_TIME.toInt()
                )
            )
            audioRecorder.startRecording()
            audioTimeRecorder?.start()
        }
        return true
    }

    override fun onPlayClicked() {
        viewModelScope.launch {
            audioPlayer.play()

            _uiState.emit(
                _uiState.value.copy(
                    isPlayButtonVisible = false,
                    isPauseButtonVisible = true,
                    isPlayTimeSeekbarVisible = true,
                    isPlayTimeVisible = true,
                    playTimeSeekbarMaxValue = _uiState.value.recordedTime.toInt()
                )
            )

            audioPlayRecorder = null
            audioPlayRecorder =
                initAudioPlayTimer(_uiState.value.recordedTime - audioPlayer.getPlayedTime())
            (audioPlayRecorder as CountDownTimer).start()
        }
    }

    override fun onPauseClicked() {
        viewModelScope.launch {
            audioPlayer.pause()
            audioPlayRecorder?.cancel()
            audioPlayRecorder = null
            _uiState.emit(
                _uiState.value.copy(
                    isPlayButtonVisible = true,
                    isPauseButtonVisible = false
                )
            )
        }
    }

    override fun onPlayVideoClicked(id: String) {
        viewModelScope.launch {
            videoPlayer.play()

            _messages.value
                .find { message -> message.id == id }
                .let { videoMessage ->
                    if (videoMessage is MessageUiModel.OutgoingVideoMessageUiModel) {
                        videoMessage.videoState = VideoMessageStates.PLAYING
                        _messages.emit(_messages.value)
                    }
                    if (videoMessage is MessageUiModel.IncomingVideoMessageUiModel) {
                        videoMessage.videoState = VideoMessageStates.PLAYING
                        _messages.emit(_messages.value)
                    }
                }
        }
    }

    override fun onPauseVideoClicked(id: String) {
        viewModelScope.launch {
            videoPlayer.pause()

            _messages.value
                .find { message -> message.id == id }
                .let { videoMessage ->
                    if (videoMessage is MessageUiModel.OutgoingVideoMessageUiModel) {
                        videoMessage.videoState = VideoMessageStates.PAUSED
                        _messages.emit(_messages.value)
                    }
                    if (videoMessage is MessageUiModel.IncomingVideoMessageUiModel) {
                        videoMessage.videoState = VideoMessageStates.PAUSED
                        _messages.emit(_messages.value)
                    }
                }
        }
    }

    override fun openFile(fileUrl: String, filePath: String) {
        viewModelScope.launch {
            _navigationFlow.emit(OpenFile(fileUrl, filePath))
        }
    }

    override fun onPermissionState(state: PermissionState) {
        when (state) {
            is GrantedState -> {
                if (state.permission == Manifest.permission.READ_EXTERNAL_STORAGE || state.permission == Manifest.permission.READ_MEDIA_IMAGES) {
                    collectInternalImages()
                }
            }

            is DeniedState -> {}
            else -> {

            }
        }
    }

    fun updateMessageBody(body: String) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(messageBody = body))
        }
    }

    fun markMessagesAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            groupChannel?.let { channel ->
                messageRepository.sendMessageReadReceipt(channel)
            }
        }
    }

    fun toggleImageSelection(imagePath: String) {
        viewModelScope.launch {
            with(_storageImages.value) {
                if (selectedImages.contains(imagePath)) {
                    _storageImages.emit(copy(selectedImages = selectedImages.minus(imagePath)))
                } else {
                    _storageImages.emit(copy(selectedImages = selectedImages.plus(imagePath)))
                }
            }
        }
    }

    fun getSelectedImageIndex(imagePath: String): Int? {
        return _storageImages.value.selectedImages.indexOf(imagePath).takeIf { it >= 0 }
    }

    fun getFileNameAndSize(uri: Uri): Pair<String, String> = fileUtils.getFileName(uri)

    fun handleFileSending(uri: Uri) {
        val mimeType = fileUtils.getMimeType(uri)
        val fileInformation = fileUtils.getFileName(uri)
        val fileName = fileInformation.first
        val fileSize = fileInformation.second

        mimeType?.let {
            when {
                mimeType.startsWith("image/") -> {
                    sendMessage(MessageUiType.Photo(uri, fileName, fileSize))
                }

                mimeType.startsWith("video/") -> {
                    sendMessage(MessageUiType.Video(uri, fileName, fileSize))
                }

                mimeType.startsWith("audio/") -> {
                    sendMessage(MessageUiType.Audio(uri, fileName, fileSize))
                }

                mimeType.startsWith("application/pdf") -> {
                    sendMessage(MessageUiType.Pdf(uri, fileName, fileSize))
                }

                mimeType.startsWith("application/msword") ||
                        mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") -> {
                    sendMessage(MessageUiType.Doc(uri, fileName, fileSize))
                }

                mimeType.startsWith("application/vnd.ms-excel") ||
                        mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") -> {
                    sendMessage(MessageUiType.Xls(uri, fileName, fileSize))
                }

                mimeType.startsWith("application/vnd.ms-powerpoint") ||
                        mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation") -> {
                    sendMessage(MessageUiType.Pptx(uri, fileName, fileSize))
                }

                else -> {
                    sendMessage(MessageUiType.File(uri, fileName, fileSize))
                }
            }
        }
    }

    /**
     * When user release the voice button, we want to stop recording audio and send message
     */
    fun stopRecording() {
        viewModelScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    isVoiceButtonVisible = false,
                    isRecordTimeVisible = false,
                    isRecordTimeSeekbarVisible = false,

                    isReplayAudioVisible = true,
                    isPlayTimeVisible = true,
                    isPlayButtonVisible = true,
                    isPauseButtonVisible = false,
                    isPlayTimeSeekbarVisible = true,
                    playTime = _uiState.value.recordedTime
                )
            )
            audioRecorder.stopRecording()
            audioTimeRecorder?.cancel()
        }
    }

    private fun initAudioPlayTimer() =
        object : CountDownTimer(MAX_AUDIO_RECORD_TIME, ONE_SECOND) {

            override fun onTick(time: Long) {
                _uiState.tryEmit(_uiState.value.copy(recordedTime = time))
            }

            override fun onFinish() {
                stopRecording()
            }
        }

    private fun initAudioPlayTimer(audioDuration: Long) =
        object : CountDownTimer(audioDuration, ONE_SECOND) {

            override fun onTick(time: Long) {
                _uiState.tryEmit(_uiState.value.copy(playTime = time))
            }

            override fun onFinish() {
                viewModelScope.launch {
                    audioPlayer.onStop()
                    _uiState.emit(
                        _uiState.value.copy(
                            isPlayButtonVisible = true,
                            isPauseButtonVisible = false,
                            playTime = ZERO.toLong()
                        )
                    )
                }
            }
        }

    fun requestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val isPermissionDenied = permissionChecker.isPermissionGranted(permission).not()
        if (isPermissionDenied) {
            viewModelScope.launch {
                _permissionState.emit(PermissionRequest(permissionsToRequest))
            }
        } else {
            collectInternalImages()
        }
    }

    private fun initUiState() {
        viewModelScope.launch {
            val header = savedStateHandle.get<RecentChatUiModel>("HEADER_MODEL")
            _uiState.emit(
                _uiState.value.copy(
                    chatUrl = header?.chatUrl ?: EMPTY,
                    contactId = header?.contactId ?: EMPTY,
                    username = header?.username ?: EMPTY,
                    profilePicture = header?.profilePicture ?: EMPTY,
                    onlineStatus = header?.onlineStatus ?: OnlineStatus.OFFLINE
                )
            )
        }
    }

    private fun collectMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            loggedUserId = userRepository.getLoggedUserId()

            GetChatMessagesUseCase(messageRepository).invoke(
                loggedUserId,
                _uiState.value.contactId
            ) { messages ->
                withContext(Dispatchers.Main) {
                    _messages.emit(messages)
                }
            }
        }
    }

    private fun initGroupChannel() {
        viewModelScope.launch(Dispatchers.IO) {
            contactsRepository.getContact(_uiState.value.chatUrl) { either ->
                either.fold({ httpError ->
                    Timber.e("http error occurred during initializing chat")
                }, { contact ->
                    groupChannel = contact
                })
            }
        }
    }

    private fun collectInternalImages() {
        viewModelScope.launch(Dispatchers.IO) {
            imagesRepository.getInternalStorageImages().foldSuspend({ readingFromStorageError ->
                Timber.e("${readingFromStorageError.errorMessage}")
            }, { images ->
                _storageImages.emit(_storageImages.value.copy(images = images))
            })
        }
    }

    private suspend fun sendTextMessage(message: String) {
        SendTextMessageUseCase(
            messageRepository,
            contactsRepository,
            recentChatsRepository,
            networkHandler
        ).invoke(
            groupChannel,
            message,
            _uiState.value.contactId
        )
    }

    private suspend fun sendFileMessage(messageType: MessageUiType?, selectedImages: List<String>) {
        val filePath = messageType?.fileUri
            ?.let { fileUtils.createFile(messageType.fileUri).path }
            ?: selectedImages.first()

        val type = messageType?.type ?: MessageUiType.Photo(null, null, null).type
        val name = messageType?.name ?: EMPTY
        val size = messageType?.size ?: EMPTY

        SendFileMessageUseCase(
            messageRepository,
            contactsRepository,
            recentChatsRepository,
            networkHandler
        ).invoke(
            groupChannel,
            filePath,
            name,
            size,
            type,
            _uiState.value.contactId
        )
    }

    companion object {
        private const val MAX_AUDIO_RECORD_TIME = 15_000L
        private const val ONE_SECOND = 1_000L
    }
}