package io.github.acedroidx.danmaku

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.ServiceRepository
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.utils.DanmakuConfigToData
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

    suspend fun updateDanmakuData(config: DanmakuConfig) {
        val data = DanmakuConfigToData.covert(config, settingsRepository) ?: return
        serviceRepository.setDanmakuData(data)
    }
}