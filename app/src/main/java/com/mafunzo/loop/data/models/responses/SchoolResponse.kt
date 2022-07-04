package com.mafunzo.loop.data.models.responses

import androidx.annotation.Keep

@Keep
data class SchoolResponse(
    var id: String? = null,
    val schoolName: String? = null,
    val schoolLocation: String? = null,
)
