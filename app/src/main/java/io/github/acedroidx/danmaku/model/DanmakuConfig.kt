package io.github.acedroidx.danmaku.model

data class DanmakuConfig(
    val msg: String,
    val mode: DanmakuMode,
    val interval: Int,
    val color: Int,
    val roomid: Int,
    val csrf: String,
    val headers: HttpHeaders,
)
