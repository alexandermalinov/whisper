package com.example.whisper.data.repository.user

import android.content.Context
import com.example.whisper.data.local.SharedPrefProvider
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.local.model.toUser
import javax.inject.Inject

class UserLocalSource @Inject constructor(
    private val context: Context,
    private val userDao: UserDao
) : UserRepository.LocalSource {

    override suspend fun updateUser(user: UserModel) {
        userDao.updateUser(user = user.toUser())
    }

    override suspend fun registerUser(user: User) {
        userDao.saveUser(user)
    }

    override suspend fun getUser(userId: String): User? = userDao.getUserById(userId)

    override suspend fun getLoggedUser(): User? {
        val email = SharedPrefProvider.getLoggedInUserEmail(context)
        return email?.let { userDao.getLoggedUser(it) }
    }

    override suspend fun setIsUserLoggedIn(isSignedIn: Boolean) {
        SharedPrefProvider.setIsUserLoggedIn(context, isSignedIn)
    }

    override suspend fun isUserLoggedIn(): Boolean = SharedPrefProvider.getIsUserLoggedIn(context)

    override suspend fun setLoggedInUserEmail(email: String) {
        SharedPrefProvider.setLoggedInUserEmail(context, email)
    }

    override suspend fun logout() {
        SharedPrefProvider.setIsUserLoggedIn(context, false)
        SharedPrefProvider.setLoggedInUserEmail(context, null)
    }
}