package com.mafunzo.loop.data.models.requests

import androidx.annotation.Keep

@Keep
data class CreateUserRequest(
    val phone: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profilePic: String,
    val dateCreated: Long,
    val accountType: String,
    val enabled: Boolean,
    val schools: HashMap<String, Boolean>
)