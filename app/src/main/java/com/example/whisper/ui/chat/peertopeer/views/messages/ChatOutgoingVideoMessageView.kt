package com.example.whisper.ui.chat.peertopeer.views.messages

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.whisper.R
import com.example.whisper.data.VideoPlayer
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.ui.chat.peertopeer.views.VideoPlayer
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiModel
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiModel.OutgoingVideoMessageUiModel.Companion.isVideoPlaying
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType
import com.example.whisper.vo.chat.peertopeer.messages.VideoMessageStates

@Composable
fun OutgoingVideoMessageView(
    messageState: MessageUiModel.OutgoingVideoMessageUiModel,
    showStatus: Boolean,
    playVideo: () -> Unit,
    pauseVideo: () -> Unit
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(VideoFrameDecoder.Factory())
        }
        .placeholder(R.drawable.ic_image_placeholder)
        .build()

    val context = LocalContext.current

    var videoPosition = remember {
        0
    }

    val videoView = remember {
        VideoView(context).apply {
            setVideoURI(Uri.parse(messageState.fileUrl))
            setOnPreparedListener { mediaPlayer ->
                //videoDuration = mediaPlayer.duration
                mediaPlayer.setOnSeekCompleteListener {
                    videoPosition = mediaPlayer.currentPosition
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Box {
             if (messageState.videoState == VideoMessageStates.INITIAL) {
                     AsyncImage(
                         model = messageState.fileUrl,
                         imageLoader = imageLoader,
                         contentDescription = "internal storage image",
                         modifier = Modifier
                             .wrapContentSize()
                             .widthIn(0.dp, 300.dp)
                             .heightIn(0.dp, 300.dp)
                             .padding(4.dp)
                             .clip(RoundedCornerShape(16.dp))
                             .clickable(onClick = {

                             }),
                         contentScale = ContentScale.Inside,
                     )
                 }

                /*AndroidView(
                    factory = { context ->
                        videoView
                    },
                    update = { videoView ->
                        videoView.setOnPreparedListener { mp ->

                        }
                        videoView.setVideoURI(Uri.parse(messageState.fileUrl))
                    }
                )*/

                IconButton(
                    modifier = Modifier
                        .background(
                            color = colorResource(id = R.color.black).copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .size(56.dp)
                        .padding(4.dp)
                        .align(Alignment.Center),
                    onClick = {
                        if (messageState.isVideoPlaying()) {
                            pauseVideo()
                        } else {
                            playVideo()
                        }
                    },
                ) {
                    Icon(
                        if (messageState.isVideoPlaying()) {
                            Icons.Rounded.PlayArrow
                        } else {
                            Icons.Rounded.MailOutline
                        },
                        contentDescription = "attachments button",
                        tint = colorResource(id = R.color.arsenic_grey)
                    )
                }
            }

            if (showStatus) {
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = when (messageState.status) {
                        MessageStatus.DELIVERED -> R.string.chat_status_delivered
                        MessageStatus.READ -> R.string.chat_status_read
                        else -> R.string.chat_status_sending
                    }.let { resource ->
                        stringResource(id = resource)
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.outgoing_message)
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewOutgoingVideoMessageView() {
    OutgoingVideoMessageView(
        MessageUiModel.OutgoingVideoMessageUiModel(
            id = "",
            fileUrl = "",
            type = MessageUiType.Video(null, null, null),
            status = MessageStatus.READ,
            createdAt = 0L
        ),
        true,
        {},
        {}
    )
}