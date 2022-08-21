package io.github.acedroidx.danmaku.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfigRepository
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(danmakuConfigRepository: DanmakuConfigRepository) :
    ViewModel() {

    val text = MutableLiveData<String>().apply { value = "This is Profiles Fragment" }
    val profiles = danmakuConfigRepository.getAllInFlow().asLiveData()

    val isRunning = MutableLiveData<Boolean>().apply { value = false }
}