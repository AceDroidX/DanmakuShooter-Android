package io.github.acedroidx.danmaku.model

import io.github.acedroidx.danmaku.R

enum class StartPage(val str: String, val id: Int) {
    HOME("home", R.id.navigation_home),
    PROFILE("profile", R.id.navigation_profiles);

    companion object {
        fun findById(id: Int): StartPage? = values().find { it.id == id }
        fun findByStr(str: String): StartPage? = values().find { it.str == str }
    }
}