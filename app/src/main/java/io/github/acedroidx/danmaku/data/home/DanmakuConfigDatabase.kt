package io.github.acedroidx.danmaku.data.home

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DanmakuConfig::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 2, to = 3)]
)
abstract class DanmakuConfigDatabase : RoomDatabase() {
    abstract fun danmakuConfigDao(): DanmakuConfigDao
}