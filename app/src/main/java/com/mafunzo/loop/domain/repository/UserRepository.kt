package com.mafunzo.loop.domain.repository

import com.mafunzo.loop.data.local.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow


interface UserRepository {
    suspend fun insertUsertoRoom(user: UserEntity)
    suspend fun getUserDetailsRoom(phoneNumber: String): Flow<UserEntity>
    suspend fun clearUserData()
}