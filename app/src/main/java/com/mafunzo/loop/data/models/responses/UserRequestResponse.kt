package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class UserRequestResponse(
    val id: String = "",
    val message: String = "",
    val subject: String = "",
    val createdAt: Long = 0,
    val status: String = "",
    val type: String = "",
) : Parcelable