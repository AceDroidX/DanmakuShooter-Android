package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.*
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.utils.CookieStrToJson
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.model.HttpHeaders
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

    init {
        danmakuConfig.addSource(roomid) { updateDanmakuConfig() }
        danmakuConfig.addSource(danmakuText) { updateDanmakuConfig() }
        danmakuConfig.addSource(danmakuInterval) { updateDanmakuConfig() }
        danmakuConfig.addSource(danmakuMultiMode) { updateDanmakuConfig() }
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

    fun saveDanmakuConfig() {
        viewModelScope.launch(Dispatchers.Main) {
            Log.d("HomeViewModel", "saveDanmakuConfig")
            danmakuConfig.value?.let {
                if (danmakuConfigRepository.getAll().isEmpty()) {
                    danmakuConfigRepository.insert(it)
                } else {
                    danmakuConfigRepository.update(it)
                }
            }
        }
    }

    fun updateDanmakuConfig() {
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
        danmakuConfig.value = DanmakuConfig(1, _text, shootMode, _interval, 9920249, _roomid)
        updateDanmakuData(danmakuConfig.value!!)
        saveDanmakuConfig()
    }

    fun updateDanmakuData(config: DanmakuConfig) {
        viewModelScope.launch {
            val cookiestr = settingsRepository.getSettings().biliCookie
            val headers = HttpHeaders(mutableListOf()).apply {
                this.headers.apply {
                    this.add("accept: application/json")
                    this.add("Content-Type: application/x-www-form-urlencoded")
                    this.add("referrer: https://live.bilibili.com/")
                    this.add("user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1 Safari/605.1.15")
                    this.add("cookie: $cookiestr")
                }
            }
            val csrf = CookieStrToJson(cookiestr).getCookieMap()["bili_jct"]
            if (csrf == null) {
                Log.w("HomeViewModel", "Cookie中无bili_jct")
                return@launch
            }
            serviceDanmakuData.value = DanmakuData(
                config.msg,
                config.shootMode,
                config.interval,
                config.color,
                config.roomid,
                csrf,
                headers,
            )
        }
    }

    fun clearLog() {
        logText.value = ""
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