package io.github.acedroidx.danmaku.data.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

enum class SettingsKey(val value: Preferences.Key<String>) {
    BILI_COOKIE(stringPreferencesKey("bili_cookie")),
    START_PAGE(stringPreferencesKey("start_page")),
}