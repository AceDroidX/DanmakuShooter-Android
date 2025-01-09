package io.github.acedroidx.danmaku.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmoticonGroup(
    val current_cover: String,
    val pkg_name: String,
    val pkg_type:Int,
    val pkg_id: Int,
    val emoticons: List<Emoticon>,
    var name : String? = null
) : Parcelable