package io.github.acedroidx.danmaku.utils

class CookieStrToJson(private val cookieStr: String) {
    fun getCookieMap(): Map<String, String> {
        val cookieJson = buildMap {
            cookieStr.split(";").forEach {
                val cookie = it.trim().split("=")
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