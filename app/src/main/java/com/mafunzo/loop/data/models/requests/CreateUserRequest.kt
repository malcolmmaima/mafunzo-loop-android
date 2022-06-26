package com.mafunzo.loop.data.models.requests

import androidx.annotation.Keep

@Keep
data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountType: String
)