package com.mafunzo.loop.data.models.requests

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class StandardRequest(
    var id: String,
    var message: String,
    var subject: String,
    var createdAt: Long,
    var status: String,
    var type: String,
) : Parcelable
