package io.github.acedroidx.danmaku.model

import io.github.acedroidx.danmaku.utils.RealRoomID
import kotlin.properties.Delegates

class DanmakuParams(
    val msg: String,
    val msgMode: Int,
    val color: Int,
    val roomid: Int,
    private val csrf: String,
) {
    var realRoomID by Delegates.notNull<Int>()
    override fun toString(): String {
        val rnd = (System.currentTimeMillis() / 1000).toInt()
        return "bubble=5&msg=${
            msg.replace(
                "\n", "\r"
            )
        }&color=$color&mode=1&dm_type=$msgMode&fontsize=25&rnd=$rnd&roomid=$realRoomID&csrf=$csrf&csrf_token=$csrf"
    }
    init {
        realRoomID = RealRoomID.get(roomid)
    }
}