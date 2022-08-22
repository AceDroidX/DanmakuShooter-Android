package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.*
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.utils.CookieStrToJson
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.model.HttpHeaders
import io.github.acedroidx.danmaku.utils.DanmakuConfigToData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(danmakuConfigRepository: DanmakuConfigRepository) :
    ViewModel() {

    private val _text = MutableLiveData<String>().apply { value = "弹幕独轮车-Android版" }
    val text: LiveData<String> = _text

    val logText: MutableLiveData<String> = MutableLiveData()

    val roomid = MutableLiveData<Int>().apply { value = 21452505 }
    val danmakuText = MutableLiveData<String>().apply { value = "" }
    val danmakuInterval = MutableLiveData<Int>().apply { value = 8000 }
    val danmakuMultiMode = MutableLiveData<Boolean>().apply { value = false }

    val danmakuConfig = danmakuConfigRepository.findByIdInFlow(1).asLiveData()
    val serviceDanmakuData = MutableLiveData<DanmakuData>()

    val isForeground = MutableLiveData<Boolean>().apply { value = false }
    val isRunning = MutableLiveData<Boolean>().apply { value = false }
    val isAddProfile = MutableLiveData<Boolean>().apply { value = false }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    fun loadDanmakuConfig() {
        Log.d("HomeViewModel", "loadDanmakuConfig")
        if (danmakuConfig.value == null) {
            Log.w("HomeViewModel", "danmakuConfig.value==null")
            return
        }
        roomid.value = danmakuConfig.value?.roomid
        danmakuText.value = danmakuConfig.value?.msg
        danmakuInterval.value = danmakuConfig.value?.interval
        danmakuMultiMode.value = danmakuConfig.value?.shootMode == DanmakuShootMode.ROLLING
    }

    suspend fun updateDanmakuConfig() {
        Log.d("HomeViewModel", "updateDanmakuConfig")
        val _roomid = roomid.value ?: return
        val _text = danmakuText.value ?: return
        val _interval = danmakuInterval.value ?: return
        val shootMode: DanmakuShootMode
        if (danmakuMultiMode.value == true) {
            shootMode = DanmakuShootMode.ROLLING
        } else {
            shootMode = DanmakuShootMode.NORMAL
        }
        val config = DanmakuConfig(1, "主页弹幕配置", _text, shootMode, _interval, 9920249, _roomid)
        updateDanmakuData(config)
        saveDanmakuConfig(config)
    }

    suspend fun updateDanmakuData(config: DanmakuConfig) {
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

    fun onFocusChange() {
        viewModelScope.launch { updateDanmakuConfig() }
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
            updateDanmakuConfig()
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