package com.mafunzo.loop.data.models.responses

import androidx.annotation.Keep
import com.mafunzo.loop.data.local.database.entities.UserEntity

@Keep
data class UserResponse(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val dateCreated: Long? = null,
    val profilePic: String? = null,
    val accountType: String? = null,
    val enabled: Boolean? = false,
    val schools: HashMap<String, Boolean>? = null,
) {
    fun toUserEntity(phoneNumber: String): UserEntity {
        return UserEntity(
            phoneNumber = phoneNumber,
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            email = email ?: "",
            dateCreated = dateCreated ?: 0,
            profilePic = profilePic ?: "",
            accountType = accountType ?: ""
        )
    }
}
