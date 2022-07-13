package com.mafunzo.loop.data.repository

import com.mafunzo.loop.data.local.database.daos.UserDao
import com.mafunzo.loop.data.local.database.entities.UserEntity
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userPrefs: AppDatasource,
) : UserRepository {

    override suspend fun insertUsertoRoom(user: UserEntity) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }

    override suspend fun getUserDetailsRoom(phoneNumber: String): Flow<UserEntity> {
        return withContext(Dispatchers.IO) {
            userDao.getUser(phoneNumber)
        }
    }

    override suspend fun clearUserData() {
        withContext(Dispatchers.IO) {
            userDao.deleteAll()
        }
    }
}