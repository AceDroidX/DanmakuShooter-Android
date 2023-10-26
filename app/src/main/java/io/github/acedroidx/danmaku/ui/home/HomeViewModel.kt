package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.DanmakuMode
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    val danmakuConfig = MutableLiveData<DanmakuConfig>()
    val isAddProfile = MutableLiveData<Boolean>().apply { value = false }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    fun getMainProfile() {
        viewModelScope.launch {
            danmakuConfigRepository.findByIdInFlow(1).collectLatest {
                Log.d("HomeViewModel", "findMainProfile$it")
                danmakuConfig.value = it ?: DanmakuConfig(
                    1,
                    "主页弹幕配置",
                    "",
                    DanmakuMode.NORMAL,
                    DanmakuShootMode.NORMAL,
                    8000,
                    14893055,
                    21452505
                )
            }
        }
    }

    fun addProfile(name: String, profile: DanmakuConfig) {
        isAddProfile.value = false
        viewModelScope.launch {
            Log.d("HomeViewModel", "addProfile:$profile")
            danmakuConfigRepository.insert(profile.copy(id = 0, name = name))
        }
    }

    suspend fun saveDanmakuConfig(config: DanmakuConfig) {
        Log.d("HomeViewModel", "saveDanmakuConfig:$config")
        danmakuConfigRepository.getAllInFlow().take(1).collectLatest {
            Log.d("HomeViewModel", "saveDanmakuConfig-getAllInFlow:${it}")
            if (it.isEmpty()) {
                danmakuConfigRepository.insert(config)
            } else {
                danmakuConfigRepository.update(config)
            }
        }
    }
}