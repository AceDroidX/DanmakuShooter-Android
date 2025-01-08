package io.github.acedroidx.danmaku.ui.widgets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.EmoticonRepository
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.EmoticonData
import io.github.acedroidx.danmaku.model.EmoticonGroup
import io.github.acedroidx.danmaku.model.EmoticonParams
import io.github.acedroidx.danmaku.model.EmoticonResult
import io.github.acedroidx.danmaku.model.HttpHeaders
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import javax.inject.Inject

@HiltViewModel
class EmoticonViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) :
    ViewModel() {
    var emoticonRepository = EmoticonRepository()
    val emoticonGroups = emoticonRepository.emoticonGroups.asLiveData()
    private suspend fun convertEmoticonData(roomid: Int): EmoticonData {
        val cookiestr = settingsRepository.getSettings().biliCookie
        val headers = HttpHeaders(mutableListOf()).apply {
            this.headers.apply {
                this.add("accept: application/json")
                this.add("referrer: https://live.bilibili.com/")
                this.add("user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1 Safari/605.1.15")
                this.add("cookie: $cookiestr")
            }
        }
        return EmoticonData(roomid, headers)
    }

    private val client = OkHttpClient()
    private val gson = Gson()

    fun getEmoticonGroups(roomid: Int) {
        viewModelScope.launch {
            emoticonRepository.setIsPrepare(false)
            emoticonRepository.cleanEmoticonGroups()
            val emoticonData = convertEmoticonData(roomid)
            val params = EmoticonParams(roomid).toString()
            val url =
                "https://api.live.bilibili.com/xlive/web-ucenter/v2/emoticon/GetEmoticons?${params}"
            val req = Request.Builder()
                .url(url)
                .get()
                .headers(emoticonData.headers.build())
                .build()
            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("EmoticonViewModel", "onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val respstr = response.body.string()
                        val emoticonResult = gson.fromJson(respstr, EmoticonResult::class.java)
                        if(emoticonResult.code != 0)
                        {
                            throw Exception("获取表情包失败: message:${emoticonResult.message}")
                        }
                        emoticonResult.data?.data?.let {
                            emoticonRepository.setEmoticonGroups(it)
                        }
                        emoticonRepository.setIsPrepare(true)

                    } catch (e: Exception) {
                        Log.e("EmoticonViewModel", "onResponse: $e")
                    }
                }
            })
        }
    }
}