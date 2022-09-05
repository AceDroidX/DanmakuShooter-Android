package io.github.acedroidx.danmaku.ui.home

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.*
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.utils.DanmakuConfigToData
import kotlinx.coroutines.flow.collectLatest
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
                danmakuConfig.value = it
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
        if (danmakuConfigRepository.getAllInFlow().asLiveData().value?.isEmpty() != true) {
            danmakuConfigRepository.update(config)
        } else {
            danmakuConfigRepository.insert(config)
        }
    }
}