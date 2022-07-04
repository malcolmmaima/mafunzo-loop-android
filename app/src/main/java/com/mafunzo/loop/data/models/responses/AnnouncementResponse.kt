package com.mafunzo.loop.data.models.responses

import androidx.annotation.Keep

@Keep
data class AnnouncementResponse (
    val announcementTitle: String? = null,
    val announcementBody: String? = null,
    val announcementTime: Long? = null,
    val announcementImage: String? = null
)

