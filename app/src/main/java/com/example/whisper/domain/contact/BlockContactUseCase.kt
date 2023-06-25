package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockContactUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onBlock: (BlockContactState) -> Unit
    ) {
        contactsRepository.blockContact(contactUrl) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onBlock.invoke(BlockContactState.ErrorState)
                }, { success ->
                    contactsRepository.blockContactDbCache(contactUrl)
                    onBlock.invoke(BlockContactState.SuccessState)
                })
            }
        }
    }
}

sealed class BlockContactState {
    object ErrorState : BlockContactState()
    object SuccessState : BlockContactState()
}

