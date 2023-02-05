package com.example.whisper.data.repository.user

import android.content.Context
import com.example.whisper.data.local.SharedPrefProvider
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.User
import javax.inject.Inject

class UserLocalSource @Inject constructor(
    private val context: Context,
    private val userDao: UserDao
) : UserRepository.LocalSource {

    override suspend fun registerUser(user: User) {
        userDao.saveUser(user)
    }

    override suspend fun getLoggedUser(): User = userDao.getLoggedUser()

    override suspend fun setIsSignedIn(isSignedIn: Boolean) {
        SharedPrefProvider.setIsUserSignedIn(context, isSignedIn)
    }

    override suspend fun isSignedIn(): Boolean = SharedPrefProvider.getIsUserSignedIn(context)
}