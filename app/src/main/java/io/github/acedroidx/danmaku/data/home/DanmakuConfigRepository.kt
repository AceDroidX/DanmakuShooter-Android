package io.github.acedroidx.danmaku.data.home

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DanmakuConfigRepository @Inject constructor(private val danmakuConfigDao: DanmakuConfigDao) {
    suspend fun getAll() = danmakuConfigDao.getAll()
    fun getAllInFlow() = danmakuConfigDao.getAllInFlow()
    suspend fun findById(id: Int) = danmakuConfigDao.findById(id)
    fun findByIdInFlow(id: Int) = danmakuConfigDao.findByIdInFlow(id)
    suspend fun insert(config: DanmakuConfig) = danmakuConfigDao.insert(config)
    suspend fun update(config: DanmakuConfig) = danmakuConfigDao.update(config)
    suspend fun delete(config: DanmakuConfig) = danmakuConfigDao.delete(config)
}