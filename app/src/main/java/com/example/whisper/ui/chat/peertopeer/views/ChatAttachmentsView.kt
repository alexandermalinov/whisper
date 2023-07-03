package com.example.whisper.ui.chat.peertopeer.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.whisper.R

@Composable
fun ChatAttachments(
    images: List<String>,
    getSelectionIndex: (String) -> Int?,
    isImageSelected: (String) -> Boolean,
    toggleImageSelection: (String) -> Unit,
    onChooseGalleryImage: () -> Unit,
    onChooseDocument: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        LazyRow {
            items(images) { image ->
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(image)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .build(),
                        contentDescription = "internal storage image",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = {
                                toggleImageSelection(image)
                            }),
                        contentScale = ContentScale.Crop,
                    )

                    val isSelected = isImageSelected(image)
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = colorResource(id = R.color.cactus_grey).copy(0.5f)
                                )
                        ) {
                            Text(
                                text = getSelectionIndex(image)?.plus(1).toString(),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, start = 12.dp, bottom = 8.dp)
                                    .background(
                                        color = colorResource(id = R.color.cactus_green),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        val attachmentsItems = listOf(
            AttachmentsItem(
                textRes = R.string.attachment_photos,
                iconRes = R.drawable.ic_images
            ) { onChooseGalleryImage() },
            AttachmentsItem(
                textRes = R.string.attachment_gif,
                iconRes = R.drawable.ic_gif
            ) {},
            AttachmentsItem(
                textRes = R.string.attachment_file,
                iconRes = R.drawable.ic_file_generic
            ) { onChooseDocument() },
            AttachmentsItem(
                textRes = R.string.attachment_location,
                iconRes = R.drawable.ic_location
            ) {}
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement
                .spacedBy(
                    space = 16.dp,
                    alignment = Alignment.CenterHorizontally
                )
        ) {
            items(attachmentsItems) { attachment ->
                AttachmentsItem(attachment)
            }
        }
    }
}

@Composable
private fun AttachmentsItem(attachmentsItem: AttachmentsItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = { attachmentsItem.onClick() },
            modifier = Modifier
                .size(height = 36.dp, width = 72.dp)
                .background(
                    color = colorResource(id = R.color.cactus_grey),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Icon(
                painter = painterResource(id = attachmentsItem.iconRes),
                tint = Color.White,
                contentDescription = "attachment icon"
            )
        }

        Text(
            text = stringResource(id = attachmentsItem.textRes),
            style = TextStyle(
                fontSize = 12.sp,
                color = colorResource(id = R.color.arsenic_grey)
            )
        )
    }
}

@Preview
@Composable
fun PreviewChatAttachments() {
    ChatAttachments(
        images = listOf("dada", " dada", "dadada", "dada"),
        getSelectionIndex = { 0 },
        isImageSelected = { false },
        toggleImageSelection = { },
        onChooseGalleryImage = { },
        onChooseDocument = {}
    )
}

data class AttachmentsItem(
    val textRes: Int,
    val iconRes: Int,
    val onClick: () -> Unit
)