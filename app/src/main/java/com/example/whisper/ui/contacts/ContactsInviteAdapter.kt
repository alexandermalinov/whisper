package com.example.whisper.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.whisper.R
import com.example.whisper.databinding.ItemContactBinding
import com.example.whisper.databinding.ItemContactInviteBinding
import com.example.whisper.utils.recyclerview.DataBoundListAdapter
import com.example.whisper.vo.contacts.ContactUiModel

class ContactsInviteAdapter(private val presenter: ContactPresenter) :
    DataBoundListAdapter<ContactUiModel, ItemContactInviteBinding>(
        object : DiffUtil.ItemCallback<ContactUiModel>() {

            override fun areItemsTheSame(
                oldItem: ContactUiModel,
                newItem: ContactUiModel
            ) = oldItem === newItem

            override fun areContentsTheSame(
                oldItem: ContactUiModel,
                newItem: ContactUiModel
            ) = oldItem == newItem
        }
    ) {

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override fun createBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemContactInviteBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_contact_invite,
            parent,
            false
        )

    override fun bind(
        binding: ItemContactInviteBinding,
        item: ContactUiModel
    ) {
        binding.model = item
        binding.presenter = presenter
    }
}