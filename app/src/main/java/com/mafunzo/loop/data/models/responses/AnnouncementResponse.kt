package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class AnnouncementResponse (
    val announcementTitle: String? = null,
    val announcementBody: String? = null,
    val announcementTime: Long? = null,
    val announcementImage: String? = null
) : Parcelable

