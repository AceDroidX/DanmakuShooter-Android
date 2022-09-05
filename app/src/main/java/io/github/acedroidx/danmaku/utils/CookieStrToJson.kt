package io.github.acedroidx.danmaku.utils

import android.util.Log

class CookieStrToJson(private val cookieStr: String) {
    fun getCookieMap(): Map<String, String> {
        val cookieJson = buildMap {
            cookieStr.split(";").forEach {
                val cookie = it.trim().split("=", limit = 2)
                if (cookie.size != 2) {
                    Log.w("CookieStrToJson", "CookieStrToJson: cookie.size != 2\n$cookie")
                    return emptyMap()
                }
                put(cookie[0], cookie[1])
            }
        }
        return cookieJson
    }

    fun getCookieJsonStr(): String {
        val cookieList = cookieStr.split(";")
        val cookieJson = StringBuilder()
        cookieJson.append("{")
        for (cookie in cookieList) {
            val cookieKeyValue = cookie.split("=")
            cookieJson.append("\"")
            cookieJson.append(cookieKeyValue[0])
            cookieJson.append("\":\"")
            cookieJson.append(cookieKeyValue[1])
            cookieJson.append("\",")
        }
        cookieJson.deleteCharAt(cookieJson.length - 1)
        cookieJson.append("}")
        return cookieJson.toString()
    }
}