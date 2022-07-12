package com.mafunzo.loop.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey val phoneNumber: String,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "date_created") val dateCreated: Long?,
    @ColumnInfo(name = "profile_pic") val profilePic: String?,
    @ColumnInfo(name = "account_type") val accountType: String?,
    @ColumnInfo(name = "schools") val schools: List<String>?
)
