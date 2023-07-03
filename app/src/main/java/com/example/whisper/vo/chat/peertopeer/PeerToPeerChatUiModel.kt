package com.example.whisper.vo.chat.peertopeer

import com.example.whisper.ui.contacts.OnlineStatus
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.ZERO

data class PeerToPeerChatUiModel(
    val chatUrl: String = EMPTY,
    val contactId: String = EMPTY,
    val username: String = EMPTY,
    val profilePicture: String = EMPTY,
    val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE,

    val messageBody: String = EMPTY,

    val isVoiceButtonVisible: Boolean = true,
    val isRecordTimeVisible: Boolean = false,
    val isRecordTimeSeekbarVisible: Boolean = false,
    val recordTimeSeekbarMaxValue: Int = ZERO,
    val recordedTime: Long = ZERO.toLong(),

    val isReplayAudioVisible: Boolean = false,
    val isPlayTimeVisible: Boolean = false,
    val isPlayButtonVisible: Boolean = false,
    val isPauseButtonVisible: Boolean = false,
    val isPlayTimeSeekbarVisible: Boolean = false,
    val playTimeSeekbarMaxValue: Int = ZERO,
    val playTime: Long = ZERO.toLong()
) {

    companion object {
        const val MAX_AUDIO_RECORD_TIME = 15_000L
    }
}
