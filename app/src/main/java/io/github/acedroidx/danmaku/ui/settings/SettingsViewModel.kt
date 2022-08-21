package io.github.acedroidx.danmaku.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _text = MutableLiveData<String>().apply {
        value = "Cookie设置"
    }
    val text: LiveData<String> = _text

    private val _biliCookie = MutableLiveData<String>().apply { value = "" }
    val biliCookie: LiveData<String> = _biliCookie
    private var fetchJob: Job? = null
    fun fetchBiliCookie() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            settingsRepository.getSettings().biliCookie.let {
                _biliCookie.value = it
            }
        }
    }

    fun saveBiliCookie(biliCookie: String) {
        viewModelScope.launch {
            settingsRepository.setSettingByKey(SettingsKey.BILI_COOKIE.value, biliCookie)
        }
    }
}