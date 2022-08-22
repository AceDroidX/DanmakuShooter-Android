package io.github.acedroidx.danmaku.ui.home

import android.util.Log
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
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _text = MutableLiveData<String>().apply { value = "弹幕独轮车-Android版" }
    val text: LiveData<String> = _text

    val logText: MutableLiveData<String> = MutableLiveData()

    val roomid = MutableLiveData<Int>().apply { value = 21452505 }
    val danmakuText = MutableLiveData<String>().apply { value = "" }
    val danmakuInterval = MutableLiveData<Int>().apply { value = 8000 }
    val danmakuMultiMode = MutableLiveData<Boolean>().apply { value = false }

    val danmakuConfig = MediatorLiveData<DanmakuConfig>()
    val serviceDanmakuData = MutableLiveData<DanmakuData>()

    val isForeground = MutableLiveData<Boolean>().apply { value = false }
    val isRunning = MutableLiveData<Boolean>().apply { value = false }
    val isAddProfile = MutableLiveData<Boolean>().apply { value = false }

    init {
        this.loadDanmakuConfig()
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    fun loadDanmakuConfig() {
        viewModelScope.launch(Dispatchers.Main) {
            Log.d("HomeViewModel", "loadDanmakuConfig")
            val config = danmakuConfigRepository.findById(1)
            if (config == null) {
                Log.i("HomeViewModel", "danmakuConfigRepository.findById(1)==null")
                return@launch
            }
            roomid.value = config.roomid
            danmakuText.value = config.msg
            danmakuInterval.value = config.interval
            danmakuMultiMode.value = config.shootMode == DanmakuShootMode.ROLLING
        }
    }

    suspend fun saveDanmakuConfig() {
        Log.d("HomeViewModel", "saveDanmakuConfig")
        danmakuConfig.value?.let {
            if (danmakuConfigRepository.getAll().isEmpty()) {
                danmakuConfigRepository.insert(it)
            } else {
                danmakuConfigRepository.update(it)
            }
        }
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
        danmakuConfig.value =
            DanmakuConfig(1, "主页配置文件", _text, shootMode, _interval, 9920249, _roomid)
        updateDanmakuData(danmakuConfig.value!!)
        saveDanmakuConfig()
    }

    suspend fun updateDanmakuData(config: DanmakuConfig) {
        val result = DanmakuConfigToData.covert(config, settingsRepository)
        if (result == null) {
            return
        }
        serviceDanmakuData.value = result
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