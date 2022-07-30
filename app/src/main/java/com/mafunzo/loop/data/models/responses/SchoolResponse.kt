package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SchoolResponse(
    var id: String? = null,
    val schoolName: String? = null,
    val schoolLocation: String? = null,
): Parcelable
