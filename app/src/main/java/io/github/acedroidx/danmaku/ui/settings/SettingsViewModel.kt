package io.github.acedroidx.danmaku.ui.settings

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.settings.SettingsKey
import io.github.acedroidx.danmaku.data.settings.SettingsModel
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.StartPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsRepository: SettingsRepository) :
    ViewModel() {

    val biliCookie = settingsRepository.biliCookie().asLiveData()

    fun saveBiliCookie(biliCookie: String) {
        viewModelScope.launch {
            settingsRepository.setSettingByKey(SettingsKey.BILI_COOKIE.value, biliCookie)
        }
    }
}