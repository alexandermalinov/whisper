package com.example.whisper.ui.chat.peertopeer.views.messages

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whisper.R
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.utils.DateTimeFormatter
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType

@Composable
fun OutgoingDocumentMessage(
    type: MessageUiType,
    timestamp: Long,
    status: MessageStatus,
    showStatus: Boolean,
    onMessageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onMessageClick() },
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = when (type) {
                                is MessageUiType.Pdf -> R.drawable.ic_file_pdf
                                is MessageUiType.Doc -> R.drawable.ic_file_word
                                is MessageUiType.Xls -> R.drawable.ic_file_excel
                                is MessageUiType.Pptx -> R.drawable.ic_file_powerpoint
                                is MessageUiType.File -> R.drawable.ic_file
                                else -> R.drawable.ic_file
                            }
                        ),
                        contentDescription = "file image"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${type.name}",
                            style = TextStyle(color = Color.White),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${
                                when (type) {
                                    is MessageUiType.Pdf -> R.string.document_type_pdf
                                    is MessageUiType.Doc -> R.string.document_type_doc
                                    is MessageUiType.Xls -> R.string.document_type_xls
                                    is MessageUiType.Pptx -> R.string.document_type_pptx
                                    is MessageUiType.File -> R.string.document_type_file
                                    else -> R.string.document_type_file
                                }.let { stringRes -> 
                                    stringResource(id = stringRes)
                                }
                            } \u2022 ${type.formatFileSize(type.size?.toLong() ?: 0L)}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.outgoing_message)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
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
fun PreviewOutgoingDocumentMessage() {
    OutgoingDocumentMessage(
        MessageUiType.Pdf(Uri.EMPTY, EMPTY, EMPTY),
        0,
        MessageStatus.DELIVERED,
        true,
        {}
    )
}