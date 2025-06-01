package io.github.acedroidx.danmaku.data

import android.util.Log
import com.google.gson.Gson
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.HttpHeaders
import io.github.acedroidx.danmaku.model.RoomIdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealRoomIDRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    private val cache = mutableMapOf<Int, Int>()

    suspend fun getRealRoomId(roomId: Int): Int {
        return cache[roomId] ?: fetchRealRoomId(roomId).also { realRoomId ->
            cache[roomId] = realRoomId
        }
    }

    private suspend fun fetchRealRoomId(roomId: Int): Int {
        val cookieStr = settingsRepository.getSettings().biliCookie
        val url =
            "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id=$roomId"
        val headers = HttpHeaders(mutableListOf()).apply {
            headers.apply {
                add("accept: application/json")
                add("referrer: https://live.bilibili.com/")
                add("user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                add("cookie: $cookieStr")
            }
        }

        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .headers(headers.build())
            .build()

        return withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body.string()
                    Log.d("RoomIdRepository", respStr)
                    val result = gson.fromJson(respStr, RoomIdResult::class.java)
                    if (result.code == 0)
                        result.data.room_info.room_id
                    else throw Exception("Failed to fetch real room ID")
                } else
                    throw Exception("Failed to fetch real room ID")
            }
        }
    }
}
