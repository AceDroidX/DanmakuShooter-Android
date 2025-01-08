package io.github.acedroidx.danmaku.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Emoticon(
    val emoji: String,
    val url: String,
    val emoticon_unique: String,
    val perm: Int
) : Parcelable