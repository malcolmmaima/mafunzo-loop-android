package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class CalendarEventResponse(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val start: Long = 0,
    val end: Long = 0,
    val location: String = "",
): Parcelable
