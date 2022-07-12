package com.mafunzo.loop.data.local.database.entities

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "user_details")
@Parcelize
@Keep
data class UserEntity(
    @PrimaryKey val phoneNumber: String,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "date_created") val dateCreated: Long?,
    @ColumnInfo(name = "profile_pic") val profilePic: String?,
    @ColumnInfo(name = "account_type") val accountType: String?
): Parcelable
