package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.settings.*
import io.github.acedroidx.danmaku.utils.CookieStrToJson
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.model.DanmakuResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _text = MutableLiveData<String>().apply { value = "弹幕独轮车-Android版" }
    val text: LiveData<String> = _text

    private val _logText = MutableLiveData<String>().apply { value = "输出日志" }
    val logText: LiveData<String> = _logText

    val roomid = MutableLiveData<Int>().apply { value = 21452505 }
    val danmakuText = MutableLiveData<String>().apply { value = "" }
    val danmakuInterval = MutableLiveData<Int>().apply { value = 8000 }
    val danmakuMultiMode = MutableLiveData<Boolean>().apply { value = false }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    fun startSendDanmaku() {
        viewModelScope.launch {
            val cookiestr = settingsRepository.getSettings().biliCookie
            val headers = Headers.Builder()
                .add("accept: application/json")
                .add("Content-Type: application/x-www-form-urlencoded")
                .add("referrer: https://live.bilibili.com/")
                .add("user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1 Safari/605.1.15")
                .add("cookie", cookiestr)
                .build()
            val csrf = CookieStrToJson(cookiestr).getCookieMap()["bili_jct"]
            if (csrf == null) {
                log("csrf为空")
                return@launch
            }
            val _roomid = roomid.value ?: return@launch
            val _text = danmakuText.value ?: return@launch
            val danmakuData = DanmakuData(_text, 9920249, _roomid, csrf)
            sendDanmaku(danmakuData, headers)
        }
    }

    fun sendDanmaku(data: DanmakuData, headers: Headers) {
        Log.d("HomeViewModel", "sendDanmaku")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val req =
                        Request.Builder().url("https://api.live.bilibili.com/msg/send")
                            .headers(headers)
                            .post(data.toString().toRequestBody())
                            .build()
                    OkHttpClient().newCall(req).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            Log.d("HomeViewModel", "onFailure")
                            log(e.stackTraceToString())
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            Log.d("HomeViewModel", "onResponse")
                            try {
                                val respstr = response.body?.string()
                                val jsondata = Gson().fromJson(respstr, DanmakuResult::class.java)
                                if (jsondata.code == 0) {
                                    log("<${data.roomid}>发送成功:${data.msg}")
                                } else {
                                    log("<${data.roomid}>发送失败:${data.msg}:$respstr")
                                }
                            } catch (e: Exception) {
                                Log.e("HomeViewModel", "onResponse", e)
                                log(e.stackTraceToString())
                            }
                        }
                    })
                }
            }
        }
    }

    fun log(text: String) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(Date())
        _logText.postValue(_logText.value + "\n[$date]$text")
    }
}