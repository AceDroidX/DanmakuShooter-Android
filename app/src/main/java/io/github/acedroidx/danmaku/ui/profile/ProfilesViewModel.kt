package io.github.acedroidx.danmaku.ui.profile

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import io.github.acedroidx.danmaku.data.settings.SettingsKey
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.utils.DanmakuConfigToData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    danmakuConfigRepository: DanmakuConfigRepository,
    settingsRepository: SettingsRepository
) :
    ViewModel() {

    val text = MutableLiveData<String>().apply { value = "This is Profiles Fragment" }
    val profiles = danmakuConfigRepository.getAllInFlow().asLiveData()
    var choseProfileId = settingsRepository.choseProfileId().asLiveData()
    val danmakuData = MutableLiveData<DanmakuData>()

    val isRunning = MutableLiveData<Boolean>().apply { value = false }

    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    fun delProfile(profile: DanmakuConfig) {
        viewModelScope.launch { danmakuConfigRepository.delete(profile) }
    }

    fun onClickCard(profile: DanmakuConfig) {
        viewModelScope.launch {
            settingsRepository.setSettingByKey(
                SettingsKey.CHOSE_PROFILE_ID.value,
                profile.id
            )
            val result = DanmakuConfigToData.covert(profile, settingsRepository) ?: return@launch
            danmakuData.value = result
        }
    }
}