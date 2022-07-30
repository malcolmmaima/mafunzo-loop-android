package com.mafunzo.loop.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mafunzo.loop.data.local.database.daos.UserDao
import com.mafunzo.loop.data.local.database.entities.UserEntity

@Database(entities = [UserEntity::class], version = 1)

abstract class MafunzoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}