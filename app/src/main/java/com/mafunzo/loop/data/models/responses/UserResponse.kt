package com.mafunzo.loop.data.models.responses

import androidx.annotation.Keep

@Keep
data class UserResponse(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val dateCreated: Long? = null,
    val profilePic: String? = null,
    val accountType: String? = null,
    val schools: List<String?>? = null,
)
