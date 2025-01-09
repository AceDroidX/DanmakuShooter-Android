package io.github.acedroidx.danmaku.data

import io.github.acedroidx.danmaku.model.EmoticonGetStatus
import io.github.acedroidx.danmaku.model.EmoticonGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmoticonRepository @Inject constructor() {
    private val _emoticonGroups: MutableStateFlow<List<EmoticonGroup>?> = MutableStateFlow(listOf())
    val emoticonGroups: StateFlow<List<EmoticonGroup>?> = _emoticonGroups

    private val _status: MutableStateFlow<EmoticonGetStatus> =
        MutableStateFlow(EmoticonGetStatus.None)
    val status: StateFlow<EmoticonGetStatus> = _status

    private val _message: MutableStateFlow<String?> = MutableStateFlow(null)
    val message: StateFlow<String?> = _message
    fun setMessage(value: String) {
        _message.value = value
    }

    fun setStatus(value: EmoticonGetStatus) {
        _status.value = value
    }

    fun setEmoticonGroups(value: List<EmoticonGroup>) {
        _emoticonGroups.value = value
    }

    fun cleanEmoticonGroups() {
        _emoticonGroups.value = listOf()
    }
}