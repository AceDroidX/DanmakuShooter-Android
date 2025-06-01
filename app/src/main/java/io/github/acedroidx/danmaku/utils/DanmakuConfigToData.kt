package io.github.acedroidx.danmaku.utils

import android.util.Log
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.data.settings.SettingsRepository
import io.github.acedroidx.danmaku.model.DanmakuData
import io.github.acedroidx.danmaku.model.HttpHeaders

class DanmakuConfigToData {
    companion object {
        suspend fun covert(
            config: DanmakuConfig, settingsRepository: SettingsRepository
        ): DanmakuData? {
            val cookiestr = settingsRepository.getSettings().biliCookie
            val headers = HttpHeaders(mutableListOf()).apply {
                this.headers.apply {
                    this.add("accept: application/json")
                    this.add("Content-Type: application/x-www-form-urlencoded")
                    this.add("referrer: https://live.bilibili.com/")
                    this.add("user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1 Safari/605.1.15")
                    this.add("cookie: $cookiestr")
                }
            }
            val csrf = CookieStrToJson(cookiestr).getCookieMap()["bili_jct"]
            if (csrf == null) {
                Log.w("DanmakuConfigToData", "Cookie中无bili_jct")
                return null
            }
            return DanmakuData(
                config.msg,
                config.msgMode,
                config.shootMode,
                config.interval,
                config.color,
                config.roomid,
                RealRoomID.get(config.roomid, settingsRepository),
                csrf,
                headers,
            )
        }
    }
}