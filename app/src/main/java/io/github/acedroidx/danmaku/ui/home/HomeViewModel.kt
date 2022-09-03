package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.*
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.utils.DanmakuConfigToData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() :
    ViewModel() {

    private val _text = MutableLiveData<String>().apply { value = "弹幕独轮车-Android版" }
    val text: LiveData<String> = _text

    val logText: MutableLiveData<String> = MutableLiveData()

    val danmakuConfig = MutableLiveData<DanmakuConfig>()
    val serviceDanmakuData = MutableLiveData<DanmakuData>()

    val isForeground = MutableLiveData<Boolean>().apply { value = false }
    val isRunning = MutableLiveData<Boolean>().apply { value = false }
    val isAddProfile = MutableLiveData<Boolean>().apply { value = false }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    fun getMainProfile() {
        viewModelScope.launch {
            danmakuConfigRepository.findByIdInFlow(1).collectLatest {
                Log.d("HomeViewModel", "findMainProfile$it")
                danmakuConfig.value = it
            }
        }
    }

    suspend fun updateDanmakuData(config: DanmakuConfig) {
        Log.d("HomeViewModel", "updateDanmakuData:$config")
        val result = DanmakuConfigToData.covert(config, settingsRepository) ?: return
        serviceDanmakuData.value = result
    }

    suspend fun saveDanmakuConfig(config: DanmakuConfig) {
        Log.d("HomeViewModel", "saveDanmakuConfig:$config")
        if (danmakuConfigRepository.getAllInFlow().asLiveData().value?.isEmpty() != true) {
            danmakuConfigRepository.update(config)
        } else {
            danmakuConfigRepository.insert(config)
        }
    }

    fun clearLog() {
        logText.value = ""
    }

    fun sendOnce() {

    }

    fun onAddProfile() {
        isAddProfile.value = true
    }

    fun addProfile(name: String) {
        isAddProfile.value = false
        viewModelScope.launch {
            danmakuConfig.value?.let {
                Log.d("HomeViewModel", "addProfile:$it")
                danmakuConfigRepository.insert(it.copy(id = 0, name = name))
            }
        }
    }

    fun startSendDanmaku() {
        viewModelScope.launch {
            isRunning.postValue(true)
            Log.d("startSendDanmaku", "开始发送弹幕")
        }
    }

    fun stopSendDanmaku() {
        viewModelScope.launch {
            isRunning.postValue(false)
            Log.d("stopSendDanmaku", "停止发送弹幕")
        }
    }
}