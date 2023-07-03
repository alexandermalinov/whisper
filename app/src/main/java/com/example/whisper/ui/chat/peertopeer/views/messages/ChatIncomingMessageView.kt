package com.example.whisper.ui.chat.peertopeer.views.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whisper.R
import com.example.whisper.utils.DateTimeFormatter

@Composable
fun IncomingTextMessage(body: String, timestamp: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(0.dp, 264.dp)
                .background(
                    color = colorResource(id = R.color.cactus_grey),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(color = Color.White, text = body)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.align(Alignment.Start),
                fontSize = 12.sp,
                color = colorResource(id = R.color.arsenic_grey),
                text = DateTimeFormatter.formatMessageDateTime(timestamp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview
@Composable
fun PreviewIncomingTextMessage() {
    IncomingTextMessage("Preview of Incoming text message", 0)
}