package io.github.acedroidx.danmaku.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmoticonGroup(
    val current_cover: String,
    val pkg_name: String,
    val emoticons: List<Emoticon>
) : Parcelable