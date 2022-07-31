package com.mafunzo.loop.data.models.responses

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SystemSettingsResponse(
    val offline: Boolean? = null,
    val maintainers: List<String>? = null
): Parcelable
