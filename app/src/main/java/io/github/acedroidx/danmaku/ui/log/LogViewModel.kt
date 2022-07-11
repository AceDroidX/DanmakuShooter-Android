package io.github.acedroidx.danmaku.ui.log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LogViewModel : ViewModel() {
    val text = MutableLiveData<String>().apply { value = "This is log Fragment" }
}