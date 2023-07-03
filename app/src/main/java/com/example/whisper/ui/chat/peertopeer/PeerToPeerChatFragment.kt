package com.example.whisper.ui.chat.peertopeer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.whisper.R
import com.example.whisper.databinding.FragmentPeerToPeerChatBinding
import com.example.whisper.ui.base.BasePermissionFragment
import com.example.whisper.utils.common.collectLatestFlow
import com.example.whisper.utils.permissions.PermissionStateHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PeerToPeerChatFragment : BasePermissionFragment<FragmentPeerToPeerChatBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: PeerToPeerChatViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectUiStates()
        collectNavigation(viewModel.navigationFlow)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(
            inflater,
            getLayoutId(),
            container,
            false
        )

        dataBinding.composeViewChat.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    ChatScreen()
                }
            }
        }

        return dataBinding.root
    }

    override fun getLayoutId(): Int = R.layout.fragment_peer_to_peer_chat

    override fun providePermissionStateHandler(): PermissionStateHandler = viewModel

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun ChatScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Toolbar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(24.dp, 24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_left),
                                tint = Color.Unspecified,
                                contentDescription = "Back navigation icon"
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp, 0.dp))
                        Text(text = "Alexander Marinov", color = Color.White, fontSize = 20.sp)
                    }
                }

                // Messages
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    reverseLayout = true,
                    userScrollEnabled = true
                ) {
                    item {
                        SentTextMessage()
                        ReceivedTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                        SentTextMessage()
                        SentTextMessage()
                        ReceivedTextMessage()
                    }
                }
            }

            // Typing Field
            var textState by remember { mutableStateOf("") }
            val maxLength = 110
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = colorResource(id = R.color.arsenic_grey)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(Color.Black),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    value = textState,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.White,
                        backgroundColor = Color.Transparent,
                        cursorColor = Color.White,
                        disabledLabelColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    onValueChange = {
                        if (it.length <= maxLength) textState = it
                    },
                    placeholder = {
                        Text(color = Color.White, text = "Write a message")
                    },
                    maxLines = 5
                )

                IconButton(
                    onClick = { },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_send),
                        tint = Color.Unspecified,
                        contentDescription = "Back navigation icon"
                    )
                }
            }
        }
    }

    @Composable
    private fun ChatToolbar() {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.size(width = 0.dp, height = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(24.dp, 24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        tint = Color.Unspecified,
                        contentDescription = "Back navigation icon"
                    )
                }
                Spacer(modifier = Modifier.size(12.dp, 0.dp))
                Text(text = "Alexander Marinov", color = Color.White, fontSize = 20.sp)
            }
        }
    }

    @Composable
    fun Messages() {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            reverseLayout = true,
            userScrollEnabled = true
        ) {
            item {
                SentTextMessage()
                ReceivedTextMessage()
                ReceivedTextMessage()
                SentTextMessage()
                SentTextMessage()
                SentTextMessage()
                ReceivedTextMessage()
                SentTextMessage()
                SentTextMessage()
                ReceivedTextMessage()
            }
        }
    }

    @Composable
    fun TypingBox() {
        var textState by remember { mutableStateOf("") }
        val maxLength = 110
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = colorResource(id = R.color.arsenic_grey)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color.Black),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                value = textState,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.White,
                    backgroundColor = Color.Transparent,
                    cursorColor = Color.White,
                    disabledLabelColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                onValueChange = {
                    if (it.length <= maxLength) textState = it
                },
                placeholder = {
                    Text(color = Color.White, text = "Write a message")
                },
                maxLines = 5
            )

            IconButton(
                onClick = { },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    tint = Color.Unspecified,
                    contentDescription = "Back navigation icon"
                )
            }
        }
    }

    @Composable
    fun SentTextMessage() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                colorResource(id = R.color.cactus_green),
                                colorResource(id = R.color.cactus_green_1)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White,
                text = "Hello! This is Whisper."
            )
            Text(
                fontSize = 12.sp,
                color = colorResource(id = R.color.arsenic_grey),
                text = "09:00 AM"
            )
        }
        Spacer(modifier = Modifier.size(width = 0.dp, height = 16.dp))
    }

    @Composable
    fun ReceivedTextMessage() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier
                    .background(
                        color = colorResource(id = R.color.cactus_grey),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White,
                text = "Hello! This is Whisper."
            )
            Text(
                fontSize = 12.sp,
                color = colorResource(id = R.color.arsenic_grey),
                text = "09:00 AM"
            )
        }
        Spacer(modifier = Modifier.size(width = 0.dp, height = 16.dp))
    }

    @Preview
    @Composable
    fun PreviewSentTextMessage() {
        SentTextMessage()
    }

    @Preview
    @Composable
    fun DefaultPreview() {
        ChatScreen()
    }

    private fun collectUiStates() {
        collectLatestFlow(viewModel.uiState) { uiState ->

        }
    }
}