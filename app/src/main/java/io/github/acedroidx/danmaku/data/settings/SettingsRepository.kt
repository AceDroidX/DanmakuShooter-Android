package io.github.acedroidx.danmaku.data.settings

import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(private val settingsLocalDataSource: SettingsLocalDataSource) {
    suspend fun getSettings(): SettingsModel {
        return settingsLocalDataSource.getSettings()
    }

    suspend fun <T> setSettingByKey(key: Preferences.Key<T>, value: T) {
        settingsLocalDataSource.setSettingByKey(key, value)
    }

    fun choseProfileId() = settingsLocalDataSource.choseProfileId()
    fun biliCookie() = settingsLocalDataSource.biliCookie()
}