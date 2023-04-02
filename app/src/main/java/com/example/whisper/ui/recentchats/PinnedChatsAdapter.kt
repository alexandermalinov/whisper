package com.example.whisper.ui.recentchats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.whisper.R
import com.example.whisper.databinding.ItemPinnedChatsBinding
import com.example.whisper.utils.recyclerview.DataBoundListAdapter
import com.example.whisper.vo.recentchats.RecentChatUiModel

class PinnedChatsAdapter(private val presenter: RecentChatPresenter) :
    DataBoundListAdapter<RecentChatUiModel, ItemPinnedChatsBinding>(
        object : DiffUtil.ItemCallback<RecentChatUiModel>() {

            override fun areItemsTheSame(
                oldItem: RecentChatUiModel,
                newItem: RecentChatUiModel
            ) = oldItem === newItem

            override fun areContentsTheSame(
                oldItem: RecentChatUiModel,
                newItem: RecentChatUiModel
            ) = oldItem.profilePicture == newItem.profilePicture &&
                    oldItem.onlineStatus == newItem.onlineStatus
        }
    ) {

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override fun createBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemPinnedChatsBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_pinned_chats,
            parent,
            false
        )

    override fun bind(
        binding: ItemPinnedChatsBinding,
        item: RecentChatUiModel
    ) {
        binding.model = item
        binding.presenter = presenter
    }
}