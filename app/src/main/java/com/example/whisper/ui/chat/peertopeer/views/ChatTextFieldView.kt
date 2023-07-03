package com.example.whisper.ui.chat.peertopeer.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.whisper.R
import com.example.whisper.ui.utils.pressClickEffect
import com.example.whisper.vo.chat.peertopeer.PeerToPeerChatUiModel

@Composable
fun ChatTextFieldView(
    state: PeerToPeerChatUiModel,
    sendMessage: () -> Unit,
    openAttachments: () -> Unit,
    updateMessageBody: (String) -> Unit,
    onTextFieldClick: () -> Unit
) {
    var isRotated by rememberSaveable { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 135f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "rotation animation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            modifier = Modifier
                .rotate(rotationAngle)
                .background(
                    color = colorResource(id = R.color.cactus_grey),
                    shape = CircleShape
                )
                .size(32.dp)
                .padding(4.dp),
            onClick = {
                isRotated = isRotated.not()
                openAttachments()
            },
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "attachments button",
                tint = colorResource(id = R.color.arsenic_grey)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(modifier = Modifier
            .border(
                border = BorderStroke(
                    width = 0.5.dp,
                    color = colorResource(id = R.color.chat_second_color)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .wrapContentWidth(),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                onTextFieldClick()
                                isRotated = isRotated.not()
                            }
                        }
                    }
                },
            value = state.messageBody,
            maxLines = 4,
            onValueChange = { updateMessageBody(it) },
            cursorBrush = SolidColor(Color.White),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 16.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        if (state.messageBody.isEmpty()) {
                            Text(
                                "Write a message",
                                style = LocalTextStyle.current.copy(
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }

                        innerTextField()
                    }

                    IconButton(
                        modifier = Modifier
                            .size(36.dp)
                            .pressClickEffect(),
                        onClick = { sendMessage() },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_send),
                            tint = Color.Unspecified,
                            contentDescription = "Back navigation icon"
                        )
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewChatTextField() {
    ChatTextFieldView(PeerToPeerChatUiModel(), {}, {}, {}, {})
}