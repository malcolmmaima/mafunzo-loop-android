package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SubjectRespone(
    val id: String? = "",
    val subjectName: String? = "",
    val startTime: Long? = 0,
    val endTime: Long? = 0,
    val dayOfWeek: Int? = 0,
    val assignedTeacher: String? = "",
): Parcelable
