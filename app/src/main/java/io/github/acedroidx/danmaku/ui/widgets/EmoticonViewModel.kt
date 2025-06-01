package io.github.acedroidx.danmaku.ui.widgets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.EmoticonRepository
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.EmoticonGetStatus
import io.github.acedroidx.danmaku.model.EmoticonParams
import io.github.acedroidx.danmaku.model.EmoticonResult
import io.github.acedroidx.danmaku.model.HttpHeaders
import io.github.acedroidx.danmaku.utils.RealRoomID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import javax.inject.Inject

@HiltViewModel
class EmoticonViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    val emoticonRepository: EmoticonRepository,
) :
    ViewModel() {
    private suspend fun getHttpHeaders(): HttpHeaders {
        val cookiestr = settingsRepository.getSettings().biliCookie
        val headers = HttpHeaders(mutableListOf()).apply {
            this.headers.apply {
                this.add("accept: application/json")
                this.add("referrer: https://live.bilibili.com/")
                this.add("user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1 Safari/605.1.15")
                this.add("cookie: $cookiestr")
            }
        }
        return headers
    }

    private val client = OkHttpClient()
    private val gson = Gson()
    private suspend fun getEmoticonGroupNameMap(): Map<Int, String>? {
        val url = "https://api.bilibili.com/x/emote/user/panel/web?business=reply"
        val headers = getHttpHeaders()
        val req = Request.Builder()
            .url(url)
            .headers(headers.build())
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(req).execute().use { response ->
                if (response.isSuccessful) {
                    val respstr = response.body.string() ?: return@withContext null
                    val jsonObject = gson.fromJson(respstr, JsonObject::class.java)
                    val nameMap = mutableMapOf<Int, String>()
                    try {
                        if (jsonObject.get("code").asInt == 0) {
                            jsonObject
                                .getAsJsonObject("data")
                                .getAsJsonArray("packages").filter { it1 ->
                                    it1.asJsonObject["type"].asInt == 3
                                }.forEach { it1 ->
                                    nameMap[it1.asJsonObject["id"].asInt] =
                                        it1.asJsonObject["text"].asString
                                }
                        }
                    } catch (e: Exception) {
                        Log.e("getEmoticonGroupNameMap", "Error: $e")
                    }
                    nameMap
                } else {
                    null
                }
            }
        }
    }

    fun getEmoticonGroups(roomid: Int) {
        viewModelScope.launch {
            emoticonRepository.setStatus(EmoticonGetStatus.Loading)
            emoticonRepository.cleanEmoticonGroups()
            val headers = getHttpHeaders()
            val params = EmoticonParams(RealRoomID.get(roomid, settingsRepository)).toString()
            val nameMap = getEmoticonGroupNameMap()
            val url =
                "https://api.live.bilibili.com/xlive/web-ucenter/v2/emoticon/GetEmoticons"
            val urlWithParams = "$url?$params"
            val req = Request.Builder()
                .url(urlWithParams)
                .get()
                .headers(headers.build())
                .build()
            client.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    emoticonRepository.setStatus(EmoticonGetStatus.GetFailed)
                    Log.e("EmoticonViewModel", "onFailure")
                    emoticonRepository.setMessage("网络请求错误")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        // 解析 json
                        val respstr = response.body.string()
                        val emoticonResult = gson.fromJson(respstr, EmoticonResult::class.java)
                        // 判断 code 如果错误就抛出异常
                        emoticonResult.message?.let { emoticonRepository.setMessage(it) }
                        if (emoticonResult.code != 0)
                            throw Exception("获取表情包失败: message:${emoticonResult.message}")
                        // 获取表情包组列表
                        emoticonResult.data?.data?.filter { it.pkg_type == 5 }
                            ?.forEach {
                                it.name = nameMap?.get(it.pkg_id)
                            }
                        emoticonResult.data?.data?.let {
                            emoticonRepository.setEmoticonGroups(it)
                        }
                        emoticonRepository.setStatus(EmoticonGetStatus.GetSuccess)

                    } catch (e: Exception) {
                        emoticonRepository.setStatus(EmoticonGetStatus.GetFailed)
                        Log.e("EmoticonViewModel", "onResponse: $e")
                    }
                }
            })
        }
    }
}