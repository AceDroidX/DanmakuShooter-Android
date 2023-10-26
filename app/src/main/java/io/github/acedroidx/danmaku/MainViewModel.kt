package io.github.acedroidx.danmaku

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun startDanmakuService(context: Context, state: Boolean, profile: DanmakuConfig) {
        viewModelScope.launch {
            covertDanmakuData(profile)?.let { data ->
                if (state) DanmakuService.startDanmakuService(
                    context, Action.START, data
                )
                else DanmakuService.startDanmakuService(context, Action.STOP)
            }
        }
    }

    suspend fun covertDanmakuData(config: DanmakuConfig): DanmakuData? {
        return DanmakuConfigToData.covert(config, settingsRepository)
    }
}