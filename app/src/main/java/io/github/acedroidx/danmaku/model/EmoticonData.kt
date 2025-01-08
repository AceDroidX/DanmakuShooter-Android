package io.github.acedroidx.danmaku.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmoticonData(
    val roomid: Int,
    val headers: HttpHeaders
) : Parcelable