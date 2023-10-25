package io.github.acedroidx.danmaku

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.data.ServiceRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DanmakuService : LifecycleService() {
    private val binder = LocalBinder()
    private var sendingThread: SendDanmakuThread? = null

    @Inject
    lateinit var serviceRepository: ServiceRepository

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): DanmakuService = this@DanmakuService
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            serviceRepository.isRunning.collect {
                when (it) {
                    true -> startSending()
                    false -> stopSending()
                }
            }
        }
        lifecycleScope.launch {
            serviceRepository.isForeground.collect {
                when (it) {
                    true -> startForeground()
                    false -> stopForeground()
                }
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
        serviceRepository.setForeground(true)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DanmakuService", "onDestroy")
        stopSending()
        serviceRepository.setRunning(false)
        serviceRepository.setForeground(false)
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
            val name = "弹幕独轮车后台服务"
//            val descriptionText = ""
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel("DanmakuService", name, importance)
//            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val notification: Notification =
            NotificationCompat.Builder(this, "DanmakuService").setContentTitle("弹幕独轮车")
                .setContentText("独轮车后台服务已开启")
                .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).build()
        startForeground(1, notification)
    }

    fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
    }

    fun startSending() {
        Log.d("DanmakuService", "startSending")
        if (serviceRepository.danmakuData.value == null) {
            Log.w("DanmakuService", "startSending: danmakuData is null")
            return
        }
        if (sendingThread?.isAlive == true) {
            Log.w("DanmakuService", "startSending: sendingThread is alive")
            return
        }
        sendingThread = SendDanmakuThread(serviceRepository.danmakuData.value!!) {
            serviceRepository.addLogText(it)
        }
        sendingThread!!.start()
    }

    fun stopSending() {
        Log.d("DanmakuService", "stopSending")
        sendingThread?.interrupt()
    }

    fun stopService() {
        Log.d("DanmakuService", "stopService")
        serviceRepository.setRunning(false)
        serviceRepository.setForeground(false)
        stopSelf()
    }

    companion object {
        fun startDanmakuService(context: Context) {
            Intent(context, DanmakuService::class.java).also { intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
}