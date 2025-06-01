package io.github.acedroidx.danmaku.utils

import android.util.Log
import com.google.gson.Gson
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.HttpHeaders
import io.github.acedroidx.danmaku.model.RoomIdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

object RealRoomID { // TODO: 重构为Repository
    private val _cache = mutableMapOf<Int, Int>()
    private val _client = OkHttpClient()
    private val _gson = Gson()
    suspend fun get(roomid: Int, settingsRepository: SettingsRepository): Int {
        if (_cache.containsKey(roomid)) {
            return _cache[roomid]!!
        } else {
            val realRoomID = fetchRealRoomID(roomid, settingsRepository)
            _cache[roomid] = realRoomID
            return realRoomID
        }
    }

    private suspend fun fetchRealRoomID(roomid: Int, settingsRepository: SettingsRepository): Int {
        val cookiestr = settingsRepository.getSettings().biliCookie
        val url =
            "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id=$roomid"
        val headers = HttpHeaders(mutableListOf()).apply {
            this.headers.apply {
                this.add("accept: application/json")
                this.add("referrer: https://live.bilibili.com/")
                this.add("user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                this.add("cookie: $cookiestr")
            }
        }
        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .headers(headers.build())
            .build()
        return withContext(Dispatchers.IO)
        {
            _client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respstr = response.body.string()
                    Log.d("fetchRealRoomID", respstr)
                    val result = _gson.fromJson(respstr, RoomIdResult::class.java)
                    if (result.code == 0)
                        result.data.room_info.room_id
                    else throw Exception("Failed to fetch real room ID")
                } else
                    throw Exception("Failed to fetch real room ID")
            }
        }

    }
}