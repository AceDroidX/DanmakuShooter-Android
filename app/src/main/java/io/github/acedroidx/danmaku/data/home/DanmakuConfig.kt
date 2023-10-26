package io.github.acedroidx.danmaku.data.home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.acedroidx.danmaku.model.DanmakuMode
import io.github.acedroidx.danmaku.model.DanmakuShootMode

@Entity
data class DanmakuConfig(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val msg: String,
    @ColumnInfo(defaultValue = "NORMAL") val msgMode: DanmakuMode,
    val shootMode: DanmakuShootMode,
    val interval: Int,
    val color: Int,
    val roomid: Int,
)
