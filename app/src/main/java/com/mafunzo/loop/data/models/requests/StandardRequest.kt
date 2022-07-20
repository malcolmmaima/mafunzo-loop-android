package com.mafunzo.loop.data.models.requests

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class StandardRequest(
    val message: String,
    val subject: String,
    val createdAt: Long,
    val status: String,
) : Parcelable
