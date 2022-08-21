package io.github.acedroidx.danmaku.data.settings

import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(private val settingsLocalDataSource: SettingsLocalDataSource) {
    suspend fun getSettings(): SettingsModel {
        return settingsLocalDataSource.getSettings()
    }

    suspend fun setSettings(settings: SettingsModel) {
        settingsLocalDataSource.setSettings(settings)
    }

    suspend fun setSettingByKey(key: Preferences.Key<String>, value: String) {
        settingsLocalDataSource.setSettingByKey(key, value)
    }
}