package com.example.whisper.ui.chat.peertopeer

import android.Manifest
import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.whisper.data.AudioPlayer
import com.example.whisper.data.AudioRecorder
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.ui.base.BaseChatViewModel
import com.example.whisper.utils.SingleLiveEvent
import com.example.whisper.utils.common.ZERO
import com.example.whisper.utils.permissions.*
import com.example.whisper.vo.chat.peertopeer.PeerToPeerChatUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class PeerToPeerChatViewModel @Inject constructor(
    private val application: Application,
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseChatViewModel(userRepository, contactsRepository), PeerToPeerChatPresenter,
    PermissionStateHandler {

    val uiState
        get() = _uiState.asStateFlow()

    val permissionLiveData: LiveData<Permission>
        get() = _permissionLiveData

    private val _uiState = MutableStateFlow(PeerToPeerChatUiModel())
    private val _permissionLiveData = SingleLiveEvent<Permission>()

    private val audioRecorder = AudioRecorder(application)
    private val audioPlayer = AudioPlayer(application)
    private var audioTimeRecorder: CountDownTimer? = null
    private var audioPlayRecorder: CountDownTimer? = null

    init {
        audioTimeRecorder = initAudioPlayTimer()
        requestPermission()
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

    override fun onPermissionState(state: PermissionState) {
        when (state) {
            is GrantedState -> {}
            is DeniedState -> {}
            else -> {

            }
        }
    }

    fun onActionDown() {

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

    private fun requestPermission() {
        _permissionLiveData.value =
            PermissionRequest(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                )
            )
    }

    companion object {
        private const val MAX_AUDIO_RECORD_TIME = 15_000L
        private const val ONE_SECOND = 1_000L
    }
}