package io.github.acedroidx.danmaku

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.data.ServiceRepository
import io.github.acedroidx.danmaku.model.Action
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

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("DanmakuService", "onStartCommand: $intent")
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return START_NOT_STICKY
        }
        when (intent?.action) {
            Action.START -> {
                startSending()
                startForeground()
                serviceRepository.setRunning(true)
                serviceRepository.setForeground(true)
            }

            Action.STOP -> {
                stopSending()
                stopForeground()
                serviceRepository.setRunning(false)
                serviceRepository.setForeground(false)
            }

            Action.PAUSE -> {
                stopSending()
                with(NotificationManagerCompat.from(this)) {
                    notify(1, buildNotification(false))
                }
                serviceRepository.setRunning(false)
            }

            Action.CONTINUE -> {
                startSending()
                with(NotificationManagerCompat.from(this)) {
                    notify(1, buildNotification(false))
                }
                serviceRepository.setRunning(true)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DanmakuService", "onDestroy")
        stopSending()
        serviceRepository.setRunning(false)
    }

    private fun startForeground() {
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
        startForeground(1, buildNotification(serviceRepository.isRunning.value))
    }

    private fun buildNotification(isRunning: Boolean): Notification {
        val closeAction = NotificationCompat.Action.Builder(
            0, "关闭", PendingIntent.getService(
                this,
                0,
                Intent(this, DanmakuService::class.java).setAction(Action.STOP),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
        val pauseAction = NotificationCompat.Action.Builder(
            0, "暂停", PendingIntent.getService(
                this,
                0,
                Intent(this, DanmakuService::class.java).setAction(Action.PAUSE),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
        val continueAction = NotificationCompat.Action.Builder(
            0, "继续", PendingIntent.getService(
                this,
                0,
                Intent(this, DanmakuService::class.java).setAction(Action.CONTINUE),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        return NotificationCompat.Builder(this, "DanmakuService")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).addAction(closeAction)
            .apply {
                if (isRunning) {
                    setSubText("运行中")
                    addAction(pauseAction)
                } else {
                    setSubText("已暂停")
                    addAction(continueAction)
                }
            }.build()
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
    }

    private fun startSending() {
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

    private fun stopSending() {
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
        fun startDanmakuService(context: Context, action: String) {
            Intent(context, DanmakuService::class.java).apply {
                this.action = action
//                putExtra(Extra.DanmakuData, danmakuData)
            }.also {
                context.startService(it)
            }
        }
    }
}