package io.github.acedroidx.danmaku.model

data class DanmakuConfig(
    val msg: String,
    val mode: DanmakuShootMode,
    val interval: Int,
    val color: Int,
    val roomid: Int,
)
