package com.example.whisper.data.repository.recentchats

import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.model.UserModel
import javax.inject.Inject

class RecentChatsLocalSource @Inject constructor(
    private val contactDao: ContactDao,
    private val userDao: UserDao,
    private var currentUser: UserModel
) : RecentChatsRepository.LocalSource {

}