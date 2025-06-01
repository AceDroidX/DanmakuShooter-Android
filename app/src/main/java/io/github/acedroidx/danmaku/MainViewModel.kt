package io.github.acedroidx.danmaku

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.RealRoomIDRepository
import io.github.acedroidx.danmaku.data.ServiceRepository
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.Action
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.utils.DanmakuConfigToData
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _text = MutableLiveData<String>().apply { value = "弹幕独轮车-Android版" }
    val text: LiveData<String> = _text

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var serviceRepository: ServiceRepository

    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    @Inject
    lateinit var realRoomIDRepository: RealRoomIDRepository

    fun startDanmakuService(context: Context, state: Boolean, profile: DanmakuConfig) {
        viewModelScope.launch {
            covertDanmakuData(profile).let {
                if (it != null) {
                    if (state) DanmakuService.startDanmakuService(
                        context, Action.START, it
                    )
                    else DanmakuService.startDanmakuService(context, Action.STOP)
                } else {
                    Toast.makeText(context, "Cookie未设置或格式错误", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    suspend fun covertDanmakuData(config: DanmakuConfig): DanmakuData? {
        return DanmakuConfigToData.covert(config, settingsRepository, realRoomIDRepository)
    }
}