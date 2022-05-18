package io.github.acedroidx.danmaku

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import io.github.acedroidx.danmaku.model.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.text.SimpleDateFormat
import java.util.*

class SendDanmakuThread(
    val config: DanmakuConfig,
    val logText: MutableLiveData<String>
) :
    Thread() {

    override fun run() {
        var danmakuList: List<String> = listOf()
        if (config.mode == DanmakuMode.NORMAL) {
            danmakuList = listOf(config.msg)
        } else if (config.mode == DanmakuMode.ROLLING) {
            danmakuList = config.msg.split("\n")
        }
        var i = 0
        Log.d("SendDanmakuThread", config.msg)
        try {
            while (!isInterrupted) {
                Log.d("SendDanmakuThread", "send")
                val danmakuData = DanmakuData(
                    danmakuList[i % danmakuList.size],
                    config.color,
                    config.roomid,
                    config.csrf
                )
                sendDanmaku(danmakuData, config.headers.build())
                sleep(config.interval.toLong())
                i++
            }
        } catch (e: InterruptedException) {
            Log.d("SendDanmakuThread", "interrupted")
        }
        Log.d("SendDanmakuThread", "end")
    }

    fun sendDanmaku(data: DanmakuData, headers: Headers) {
        val req =
            Request.Builder().url("https://api.live.bilibili.com/msg/send")
                .headers(headers)
                .post(data.toString().toRequestBody())
                .build()
        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("HomeViewModel", "onFailure")
                log(e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
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

    fun log(text: String) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(Date())
        Log.d("SendDanmakuRunnable", "[$date]$text")
        logText.postValue(logText.value + "[$date]$text\n")
    }
}