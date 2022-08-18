package io.github.acedroidx.danmaku.model

data class DanmakuData (
    val msg: String,
    val shootMode: DanmakuShootMode,
    val interval: Int,
    val color: Int,
    val roomid: Int,
    val csrf: String,
    val headers: HttpHeaders,
)
