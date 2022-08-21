package io.github.acedroidx.danmaku.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.acedroidx.danmaku.model.StartPage
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsLocalDataSource @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    suspend fun getSettings(): SettingsModel {
        try {
            return SettingsModel(
                StartPage.findByStr(
                    context.settingsDataStore.data.first()[SettingsKey.START_PAGE.value]
                        ?: StartPage.HOME.str
                ) ?: StartPage.HOME,
                context.settingsDataStore.data.first()[SettingsKey.BILI_COOKIE.value] ?: ""
            )
        } catch (error: NoSuchElementException) {
            return SettingsModel(StartPage.HOME, "")
        }
    }

    suspend fun setSettings(settings: SettingsModel) {
        context.settingsDataStore.edit { preferences ->
            preferences[SettingsKey.BILI_COOKIE.value] = settings.biliCookie
            preferences[SettingsKey.START_PAGE.value] = settings.startPage.str
        }
    }

    suspend fun setSettingByKey(key: Preferences.Key<String>, value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}