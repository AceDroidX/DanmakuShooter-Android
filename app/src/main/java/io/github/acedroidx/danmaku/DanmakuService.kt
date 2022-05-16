package io.github.acedroidx.danmaku

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.model.HttpHeaders

class DanmakuService : Service() {
    val danmakuData: MutableLiveData<DanmakuData> = MutableLiveData()
    val httpHeaders: MutableLiveData<HttpHeaders> = MutableLiveData()
    val _logText = MutableLiveData<String>().apply { value = "输出日志" }
    val logText: LiveData<String> = _logText
    private val binder = LocalBinder()
    var isRunning: MutableLiveData<Boolean> = MutableLiveData()
    var sendingThread: SendDanmakuThread? = null

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): DanmakuService = this@DanmakuService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (isRunning) {
//            return START_NOT_STICKY
//        }
//        isRunning = true
//        val danmakuData = intent?.extras?.getParcelable<DanmakuData>("danmaku")
//        val headers = intent?.extras?.getParcelable<HttpHeaders>("headers")
//        if (danmakuData == null || headers == null) {
//            Log.w("DanmakuService", "onStartCommand: danmakuData or headers is null")
//            return START_NOT_STICKY
//        }
//        Log.d("DanmakuService", "onStartCommand: $danmakuData")
//        Thread(SendDanmakuRunnable(danmakuData, headers)).start()
        Log.d("DanmakuService", "onStartCommand: $intent")
        isRunning.observeForever { isRunning ->
            if (isRunning) {
                startSending()
            } else {
                stopSending()
            }
        }
        return START_NOT_STICKY
    }

    fun startSending() {
        Log.d("DanmakuService", "startSending")
        if (danmakuData.value == null || httpHeaders.value == null) {
            Log.w("DanmakuService", "startSending: danmakuData or headers is null")
            return
        }
        if (sendingThread?.isAlive == true) {
            Log.w("DanmakuService", "startSending: sendingThread is alive")
            return
        }
        sendingThread = SendDanmakuThread(danmakuData.value!!, httpHeaders.value!!, _logText)
        sendingThread!!.start()
    }

    fun stopSending() {
        Log.d("DanmakuService", "stopSending")
        if (sendingThread == null) {
            Log.w("DanmakuService", "stopSending: sendingThread is null")
            return
        }
        sendingThread!!.interrupt()
    }
}