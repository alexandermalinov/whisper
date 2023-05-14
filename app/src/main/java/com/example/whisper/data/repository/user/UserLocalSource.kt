package com.example.whisper.data.repository.user

import android.content.Context
import com.example.whisper.data.local.SharedPrefProvider
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.entity.toContactModels
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserLocalSource @Inject constructor(
    private val context: Context,
    private val userDao: UserDao,
    private val userModel: UserModel
) : UserRepository.LocalSource {

    override suspend fun registerUser(user: User) {
        userDao.saveUser(user)
    }

    override suspend fun getLoggedUser(): User? {
        val email = SharedPrefProvider.getLoggedInUserEmail(context)
        return email?.let { userDao.getLoggedUser(it) }
    }

    override fun getPinnedContacts(email: String): Flow<List<ContactModel>> =
        userDao.getUserFlow(email).map { it.pinnedContacts.toContactModels() }

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