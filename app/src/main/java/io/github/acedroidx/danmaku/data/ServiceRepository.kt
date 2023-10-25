package io.github.acedroidx.danmaku.data

import io.github.acedroidx.danmaku.model.DanmakuData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor() {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground
    private val _danmakuData: MutableStateFlow<DanmakuData?> = MutableStateFlow(null)
    val danmakuData: StateFlow<DanmakuData?> = _danmakuData
    private val _logText = MutableStateFlow("")
    val logText: StateFlow<String> = _logText

    fun setRunning(value: Boolean) {
        _isRunning.value = value
    }

    fun setForeground(value: Boolean) {
        _isForeground.value = value
    }

    fun setDanmakuData(value: DanmakuData) {
        _danmakuData.value = value
    }

    fun addLogText(value: String) {
        _logText.value += value
    }

    fun clearLogText() {
        _logText.value = ""
    }
}