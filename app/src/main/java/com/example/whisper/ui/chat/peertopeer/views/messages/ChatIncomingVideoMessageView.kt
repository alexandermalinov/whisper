package com.example.whisper.ui.chat.peertopeer.views.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.whisper.R

@Composable
fun IncomingVideoMessage(fileUrl: String) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(VideoFrameDecoder.Factory())
        }
        .placeholder(R.drawable.ic_image_placeholder)
        .build()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box {
            AsyncImage(
                model = fileUrl,
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

            IconButton(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = colorResource(id = R.color.black).copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .size(56.dp)
                    .padding(4.dp),
                onClick = {},
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "attachments button",
                    tint = colorResource(id = R.color.arsenic_grey)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewIncomingVideoMessage() {
    IncomingVideoMessage("Preview of Outgoing text message")
}