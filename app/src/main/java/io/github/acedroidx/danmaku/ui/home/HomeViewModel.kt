package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.settings.*
import io.github.acedroidx.danmaku.model.DanmakuConfig
import io.github.acedroidx.danmaku.utils.CookieStrToJson
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.model.DanmakuMode
import io.github.acedroidx.danmaku.model.HttpHeaders
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

    val serviceDanmakuConfig = MediatorLiveData<DanmakuConfig>()

    val isServiceStarted = MutableLiveData<Boolean>().apply { value = false }
    val isRunning = MutableLiveData<Boolean>().apply { value = false }

    init {
        serviceDanmakuConfig.addSource(roomid) { updateDanmakuConfig() }
        serviceDanmakuConfig.addSource(danmakuText) { updateDanmakuConfig() }
        serviceDanmakuConfig.addSource(danmakuInterval) { updateDanmakuConfig() }
        serviceDanmakuConfig.addSource(danmakuMultiMode) { updateDanmakuConfig() }
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    fun updateDanmakuConfig() {
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
            val _roomid = roomid.value ?: return@launch
            val _text = danmakuText.value ?: return@launch
            val _interval = danmakuInterval.value ?: return@launch
            val danmakuData = DanmakuData(_text, 9920249, _roomid, csrf)
            var mode: DanmakuMode
            if (danmakuMultiMode.value == true) {
                mode = DanmakuMode.ROLLING
            } else {
                mode = DanmakuMode.NORMAL
            }
            serviceDanmakuConfig.value = DanmakuConfig(
                _text,
                mode,
                _interval,
                9920249,
                _roomid,
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