package com.example.whisper.ui.chat.peertopeer.views.messages

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
import com.example.whisper.utils.DateTimeFormatter
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType

@Composable
fun IncomingDocumentMessage(type: MessageUiType, timestamp: Long, onMessageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onMessageClick() },
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
                            color = colorResource(id = R.color.incoming_message)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
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
fun PreviewIncomingDocumentMessage() {
    IncomingDocumentMessage(MessageUiType.Pdf(null, null, null), 0, {})
}