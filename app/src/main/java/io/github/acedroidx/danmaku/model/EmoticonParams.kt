package io.github.acedroidx.danmaku.model

class EmoticonParams(
    val roomid: Int
) {
    override fun toString(): String {
        return "platform=pc&room_id=$roomid"
    }
}