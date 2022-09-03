package io.github.acedroidx.danmaku.ui.profile

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor() :
    ViewModel() {
    @Inject
    lateinit var danmakuConfigRepository: DanmakuConfigRepository

    var profile = MutableLiveData<DanmakuConfig>()

    fun findProfileById(id: Int) {
        viewModelScope.launch {
            danmakuConfigRepository.findByIdInFlow(id).collectLatest { profile.value = it }
        }
    }

    suspend fun saveProfile(profile: DanmakuConfig) {
        danmakuConfigRepository.update(profile)
    }
}