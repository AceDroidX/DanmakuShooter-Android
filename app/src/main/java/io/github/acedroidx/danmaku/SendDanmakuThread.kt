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
    val data: DanmakuData,
    val logText: MutableLiveData<String>
) :
    Thread() {

    override fun run() {
        var danmakuList: List<String> = listOf()
        if (data.shootMode == DanmakuShootMode.NORMAL) {
            danmakuList = listOf(data.msg)
        } else if (data.shootMode == DanmakuShootMode.ROLLING) {
            danmakuList = data.msg.split("\n")
        }
        var i = 0
        Log.d("SendDanmakuThread", data.msg)
        try {
            while (!isInterrupted) {
                Log.d("SendDanmakuThread", "send")
                val danmakuData = DanmakuParams(
                    danmakuList[i % danmakuList.size],
                    data.color,
                    data.roomid,
                    data.csrf
                )
                sendDanmaku(danmakuData, data.headers.build())
                sleep(data.interval.toLong())
                i++
            }
        } catch (e: InterruptedException) {
            Log.d("SendDanmakuThread", "interrupted")
        }
        Log.d("SendDanmakuThread", "end")
    }

    fun sendDanmaku(params: DanmakuParams, headers: Headers) {
        val req =
            Request.Builder().url("https://api.live.bilibili.com/msg/send")
                .headers(headers)
                .post(params.toString().toRequestBody())
                .build()
        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("HomeViewModel", "onFailure")
                log(e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("HomeViewModel", "onResponse")
                try {
                    val respstr = response.body.string()
                    val jsondata = Gson().fromJson(respstr, DanmakuResult::class.java)
                    if (jsondata.code == 0) {
                        if (jsondata.msg != null && jsondata.msg != "") {
                            log("<${params.roomid}>发送异常:${params.msg}:$respstr")
                        } else if (jsondata.message != null && jsondata.message != "") {
                            log("<${params.roomid}>发送异常:${params.msg}:$respstr")
                        } else {
                            log("<${params.roomid}>发送成功:${params.msg}")
                        }
                    } else {
                        log("<${params.roomid}>发送失败:${params.msg}:$respstr")
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