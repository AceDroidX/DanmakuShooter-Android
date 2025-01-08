package io.github.acedroidx.danmaku.data

import io.github.acedroidx.danmaku.model.EmoticonGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmoticonRepository @Inject constructor() {
    private val _emoticonGroups :MutableStateFlow<List<EmoticonGroup>> = MutableStateFlow(listOf())
    val emoticonGroups : StateFlow<List<EmoticonGroup>> = _emoticonGroups

    private val _isPrepare : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPrepare : StateFlow<Boolean> = _isPrepare
    fun setIsPrepare(value: Boolean) {
        _isPrepare.value = value
    }
    fun setEmoticonGroups(value: List<EmoticonGroup>) {
        _emoticonGroups.value = value
    }

    fun cleanEmoticonGroups() {
        _emoticonGroups.value = listOf()
    }
}