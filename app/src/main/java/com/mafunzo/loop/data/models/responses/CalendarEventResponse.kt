package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

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
