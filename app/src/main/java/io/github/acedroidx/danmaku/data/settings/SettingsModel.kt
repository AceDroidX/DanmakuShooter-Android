package io.github.acedroidx.danmaku.data.settings

import io.github.acedroidx.danmaku.model.StartPage

data class SettingsModel(
    val startPage: StartPage,
    val choseProfileId: Int,
    val biliCookie: String,
)
