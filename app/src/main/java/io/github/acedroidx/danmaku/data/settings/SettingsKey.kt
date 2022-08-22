package io.github.acedroidx.danmaku.data.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

sealed class SettingsKey<T>(val value: Preferences.Key<T>) {
    object BILI_COOKIE : SettingsKey<String>(stringPreferencesKey("bili_cookie"))
    object START_PAGE : SettingsKey<String>(stringPreferencesKey("start_page"))
    object CHOSE_PROFILE_ID : SettingsKey<Int>(intPreferencesKey("chose_profile_id"))
}