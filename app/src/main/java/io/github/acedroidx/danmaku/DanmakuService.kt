package io.github.acedroidx.danmaku

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.acedroidx.danmaku.model.DanmakuConfig

class DanmakuService : LifecycleService() {
    val danmakuConfig: MutableLiveData<DanmakuConfig> = MutableLiveData()
    val logText: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    private val binder = LocalBinder()
    var isRunning: MutableLiveData<Boolean> = MutableLiveData()
    val isForeground: MutableLiveData<Boolean> = MutableLiveData()
    var sendingThread: SendDanmakuThread? = null

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): DanmakuService = this@DanmakuService
    }

    override fun onCreate() {
        super.onCreate()
        isRunning.observe(this) { isRunning ->
            if (isRunning) {
                startSending()
            } else {
                stopSending()
            }
        }
        isForeground.observe(this) {
            if (it) {
                startForeground()
            } else {
                stopForeground()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("DanmakuService", "onStartCommand: $intent")
        isForeground.value = true
        return START_NOT_STICKY
    }

    fun startForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                val intentFlags: Int
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intentFlags = PendingIntent.FLAG_IMMUTABLE
                } else {
                    intentFlags = 0
                }
                PendingIntent.getActivity(this, 0, notificationIntent, intentFlags)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "???????????????????????????"
//            val descriptionText = ""
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("DanmakuService", name, importance)
//            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val notification: Notification = NotificationCompat.Builder(this, "DanmakuService")
            .setContentTitle("???????????????")
            .setContentText("??????????????????????????????")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
    }

    fun stopForeground() {
        stopForeground(true)
    }

    fun startSending() {
        Log.d("DanmakuService", "startSending")
        if (danmakuConfig.value == null) {
            Log.w("DanmakuService", "startSending: danmakuConfig is null")
            return
        }
        if (sendingThread?.isAlive == true) {
            Log.w("DanmakuService", "startSending: sendingThread is alive")
            return
        }
        sendingThread = SendDanmakuThread(danmakuConfig.value!!, logText)
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

    fun stopService() {
        Log.d("DanmakuService", "stopService")
        isRunning.value = false
        isForeground.value = false
        stopSelf()
    }
}