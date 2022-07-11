package io.github.acedroidx.danmaku.model

class DanmakuParams(
    val msg: String,
    val color: Number,
    val roomid: Number,
    private val csrf: String
) {
    override fun toString(): String {
        val rnd = (System.currentTimeMillis() / 1000).toInt()
        return "bubble=5&msg=${
            msg.replace(
                "\n",
                "\r"
            )
        }&color=$color&mode=1&fontsize=25&rnd=$rnd&roomid=$roomid&csrf=$csrf&csrf_token=$csrf"
    }
}