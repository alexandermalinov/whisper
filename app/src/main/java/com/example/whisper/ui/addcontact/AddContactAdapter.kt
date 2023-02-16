package com.example.whisper.ui.addcontact

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.whisper.R
import com.example.whisper.databinding.ItemAddContactBinding
import com.example.whisper.utils.recyclerview.DataBoundListAdapter
import com.example.whisper.vo.contacts.ContactUiModel

class AddContactAdapter(private val presenter: AddContactPresenter) :
    DataBoundListAdapter<ContactUiModel, ItemAddContactBinding>(
        object : DiffUtil.ItemCallback<ContactUiModel>() {

            override fun areItemsTheSame(
                oldItem: ContactUiModel,
                newItem: ContactUiModel
            ) = oldItem.contactId == newItem.contactId

            override fun areContentsTheSame(
                oldItem: ContactUiModel,
                newItem: ContactUiModel
            ) = oldItem.contactId == newItem.contactId
        }
    ) {

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override fun createBinding(
        parent: ViewGroup,
        viewType: Int
    ): ItemAddContactBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_add_contact,
            parent,
            false
        )

    override fun bind(
        binding: ItemAddContactBinding,
        item: ContactUiModel
    ) {
        binding.model = item
        binding.presenter = presenter
    }
}