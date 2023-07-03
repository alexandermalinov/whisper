package com.example.whisper.ui.chat.peertopeer.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.whisper.R
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.ui.contacts.OnlineStatus
import com.example.whisper.vo.chat.peertopeer.PeerToPeerChatUiModel

@Composable
fun ChatToolbar(state: PeerToPeerChatUiModel, onBackClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onBackClicked() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    tint = Color.Unspecified,
                    contentDescription = "Back navigation icon"
                )
            }

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = state.username,
                color = Color.White,
                fontSize = 18.sp
            )

            Box {
                Image(
                    painter = rememberAsyncImagePainter(state.profilePicture),
                    contentDescription = "avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )

                if (state.onlineStatus == OnlineStatus.ONLINE) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .border(1.dp, Color.Black, CircleShape)
                            .padding(1.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }

        Divider(color = colorResource(id = R.color.chat_top_bottom_color), thickness = 1.dp)
    }
}

@Preview
@Composable
fun PreviewChatToolbar() {
    ChatToolbar(PeerToPeerChatUiModel()) {  }
}