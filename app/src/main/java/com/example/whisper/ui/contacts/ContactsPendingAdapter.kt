package com.example.whisper.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.whisper.R
import com.example.whisper.databinding.ItemContactPendingBinding
import com.example.whisper.utils.recyclerview.DataBoundListAdapter
import com.example.whisper.vo.contacts.ContactUiModel

class ContactsPendingAdapter(private val presenter: ContactPresenter) :
    DataBoundListAdapter<ContactUiModel, ItemContactPendingBinding>(
        object : DiffUtil.ItemCallback<ContactUiModel>() {

            override fun areItemsTheSame(
                oldItem: ContactUiModel,
                newItem: ContactUiModel
            ) = oldItem.channelUrl == newItem.channelUrl

            override fun areContentsTheSame(
                oldItem: ContactUiModel,
                newItem: ContactUiModel
            ) = oldItem.pictureUrl == newItem.pictureUrl &&
                    oldItem.username == newItem.username &&
                    oldItem.isInvited == newItem.isInvited &&
                    oldItem.isMuted == newItem.isMuted &&
                    oldItem.isPinned == newItem.isPinned &&
                    oldItem.isLoading == newItem.isLoading
        }
    ) {

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override fun createBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemContactPendingBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_contact_pending,
            parent,
            false
        )

    override fun bind(
        binding: ItemContactPendingBinding,
        item: ContactUiModel
    ) {
        binding.model = item
        binding.presenter = presenter
    }
}