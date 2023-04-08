package com.example.whisper.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.whisper.R
import com.example.whisper.databinding.ItemContactBinding
import com.example.whisper.utils.recyclerview.DataBoundListAdapter
import com.example.whisper.vo.contacts.ContactUiModel

class ContactsAdapter(private val presenter: ContactPresenter) :
    DataBoundListAdapter<ContactUiModel, ItemContactBinding>(
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
    ): ItemContactBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_contact,
            parent,
            false
        )

    override fun bind(
        binding: ItemContactBinding,
        item: ContactUiModel
    ) {
        binding.model = item
        binding.presenter = presenter
    }
}