package com.mafunzo.loop.data.local.database.daos

import androidx.room.*
import com.mafunzo.loop.data.local.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    //get user by phoneNumber
    @Query("SELECT * FROM user_details WHERE phoneNumber = :phoneNumber")
    fun getUser(phoneNumber: String): Flow<UserEntity>

    //insert user
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity)

    //update user
    @Update
    suspend fun updateUser(user: UserEntity)

    //delete user where phoneNumber = :phoneNumber
    @Query("DELETE FROM user_details WHERE phoneNumber = :phoneNumber")
    suspend fun deleteUser(phoneNumber: String)

    //delete all user data
    @Query("DELETE FROM user_details")
    suspend fun deleteAll()
}