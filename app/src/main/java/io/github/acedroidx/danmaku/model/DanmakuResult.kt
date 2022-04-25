package io.github.acedroidx.danmaku.model

data class DanmakuResult(
    var code: Int,
    var message: String? = null,
    var msg: String? = null,
    var data: Any? = null
)