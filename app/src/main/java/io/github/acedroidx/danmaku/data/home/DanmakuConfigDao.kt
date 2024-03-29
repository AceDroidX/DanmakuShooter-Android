package io.github.acedroidx.danmaku.data.home

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DanmakuConfigDao {
    @Query("SELECT * FROM danmakuconfig")
    suspend fun getAll(): List<DanmakuConfig>

    @Query("SELECT * FROM danmakuconfig")
    fun getAllInFlow(): Flow<List<DanmakuConfig>>

    @Query("SELECT * FROM danmakuconfig WHERE id=:id")
    suspend fun findById(id: Int): DanmakuConfig?

    @Query("SELECT * FROM danmakuconfig WHERE id=:id")
    fun findByIdInFlow(id: Int): Flow<DanmakuConfig?>

    @Insert
    suspend fun insert(config: DanmakuConfig)

    @Update
    suspend fun update(config: DanmakuConfig)

    @Delete
    suspend fun delete(config: DanmakuConfig)
}