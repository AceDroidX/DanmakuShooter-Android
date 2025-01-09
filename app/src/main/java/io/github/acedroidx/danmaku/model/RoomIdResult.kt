package io.github.acedroidx.danmaku.model

data class RoomIdResult(
    val code: Int,
    val message: String,
    val data: RoomData
) {
    data class RoomData(
        val room_info: RoomInfo
    ) {
        data class RoomInfo(
            val room_id: Int
        )
    }
}