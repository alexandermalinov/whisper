package com.example.whisper.ui.chat.peertopeer.views

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

@Composable
fun VideoPlayer(filePath: String) {
    // This is the official way to access current context from Composable functions
    val context = LocalContext.current

    // Do not recreate the player everytime this Composable commits
    val exoPlayer = remember(context) {
        SimpleExoPlayer.Builder(context).build().apply {
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.packageName)
            )

            val source = ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(filePath))

            this.prepare(source)
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .widthIn(0.dp, 300.dp)
            .heightIn(0.dp, 300.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        DisposableEffect(key1 = Unit) { onDispose { exoPlayer.release() } }

        AndroidView(
            factory = {
                StyledPlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                }
            }
        )
    }

    // Remember the VideoView
    val videoView = remember {
        VideoView(context).apply {
            setVideoURI(Uri.parse(filePath))
            setOnPreparedListener { mediaPlayer ->
                //videoDuration = mediaPlayer.duration
                mediaPlayer.setOnSeekCompleteListener {
                    //videoPosition = mediaPlayer.currentPosition
                }
            }
        }
    }
}