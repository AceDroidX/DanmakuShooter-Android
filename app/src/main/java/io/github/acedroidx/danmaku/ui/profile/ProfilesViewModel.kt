package io.github.acedroidx.danmaku.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor() : ViewModel() {
    val text = MutableLiveData<String>().apply { value = "This is Profiles Fragment" }

    val isRunning = MutableLiveData<Boolean>().apply { value = false }
}