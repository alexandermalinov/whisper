package com.example.whisper.ui.chat.peertopeer.views.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whisper.R
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.utils.DateTimeFormatter

@Composable
fun OutgoingTextMessage(body: String, timestamp: Long, status: MessageStatus, showStatus: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Column(
                modifier = Modifier
                    .widthIn(0.dp, 264.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                colorResource(id = R.color.cactus_green2),
                                colorResource(id = R.color.cactus_green_1)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(color = Color.White, text = body)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.align(Alignment.End),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.outgoing_message)
                    ),
                    text = DateTimeFormatter.formatMessageDateTime(timestamp)
                )
            }

            if (showStatus) {
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = when (status) {
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
fun PreviewOutgoingTextMessage() {
    OutgoingTextMessage("Preview of Outgoing text message", 0, MessageStatus.DELIVERED, true)
}