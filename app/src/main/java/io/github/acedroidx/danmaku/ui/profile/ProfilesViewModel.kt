package io.github.acedroidx.danmaku.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfilesViewModel : ViewModel() {
    val text = MutableLiveData<String>().apply { value = "This is Profiles Fragment" }

    val isRunning = MutableLiveData<Boolean>().apply { value = false }
}