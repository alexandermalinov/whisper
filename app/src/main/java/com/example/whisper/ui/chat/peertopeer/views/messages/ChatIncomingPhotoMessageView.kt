package com.example.whisper.ui.chat.peertopeer.views.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.whisper.R

@Composable
fun IncomingPhotoMessage(fileUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fileUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .build(),
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
    }
}

@Preview
@Composable
fun PreviewIncomingFileMessage() {
    IncomingPhotoMessage("Preview of Outgoing text message")
}