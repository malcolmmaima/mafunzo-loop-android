package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class TeachersResponse(
    val id: String? = "",
    val firstName: String? = "",
    val lastName: String? = "",
    val phoneNumber: String? = "",
    val emailAddress: String? = "",
    val bio: String? = "",
    val dateCreated: Long? = 0,
    val profilePic: String? = "",
    val status: String? = "",
    val grades: List<String>? = emptyList(),
    val subjects: List<String>? = emptyList(),
) : Parcelable
