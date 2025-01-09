package io.github.acedroidx.danmaku.model

import io.github.acedroidx.danmaku.utils.RealRoomID

class EmoticonParams(
    val roomid: Int
) {
    override fun toString(): String {
        return "platform=pc&room_id=${RealRoomID.get(roomid)}"
    }
}