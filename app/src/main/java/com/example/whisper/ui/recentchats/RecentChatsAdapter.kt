package com.example.whisper.ui.recentchats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.whisper.R
import com.example.whisper.databinding.ItemRecentChatBinding
import com.example.whisper.utils.recyclerview.DataBoundListAdapter
import com.example.whisper.vo.recentchats.RecentChatUiModel

class RecentChatsAdapter(private val presenter: RecentChatPresenter) :
    DataBoundListAdapter<RecentChatUiModel, ItemRecentChatBinding>(
        object : DiffUtil.ItemCallback<RecentChatUiModel>() {

            override fun areItemsTheSame(
                oldItem: RecentChatUiModel,
                newItem: RecentChatUiModel
            ) = oldItem.chatUrl == newItem.chatUrl

            override fun areContentsTheSame(
                oldItem: RecentChatUiModel,
                newItem: RecentChatUiModel
            ) = oldItem == newItem &&
                    oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.username == newItem.username &&
                    oldItem.lastMessageText == newItem.lastMessageText &&
                    oldItem.lastMessageTimestamp == newItem.lastMessageTimestamp &&
                    oldItem.onlineStatus == newItem.onlineStatus &&
                    oldItem.unreadMessagesCount == newItem.unreadMessagesCount &&
                    oldItem.isMuted == newItem.isMuted &&
                    oldItem.isPinned == newItem.isPinned
        }
    ) {

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override fun createBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemRecentChatBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_recent_chat,
            parent,
            false
        )

    override fun bind(
        binding: ItemRecentChatBinding,
        item: RecentChatUiModel
    ) {
        binding.model = item
        binding.presenter = presenter
    }
}