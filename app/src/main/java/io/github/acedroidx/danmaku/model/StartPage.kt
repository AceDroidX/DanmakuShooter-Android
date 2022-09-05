package io.github.acedroidx.danmaku.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.acedroidx.danmaku.R

enum class StartPage(
    val route: String,
    val displayName: String,
    @StringRes val id: Int,
    @DrawableRes val icon: Int
) {
    HOME("home", "Home", R.id.navigation_home, R.drawable.ic_home_black_24dp),
    PROFILE("profile", "配置", R.id.navigation_profiles, R.drawable.ic_file_document_24dp),
    LOG("log", "日志", R.id.navigation_log, R.drawable.ic_bug_24dp),
    SETTING("setting", "设置", R.id.navigation_notifications, R.drawable.ic_cog_black_24dp);

    companion object {
        fun findById(id: Int): StartPage? = values().find { it.id == id }
        fun findByStr(str: String): StartPage? = values().find { it.route == str }
    }
}